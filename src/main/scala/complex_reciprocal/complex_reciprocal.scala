// Dsp-block complex_reciprocal
// Description here 
// Inititally written by dsp-blocks initmodule.sh, 20190425
package complex_reciprocal

import chisel3.experimental._
import chisel3._
import chisel3.iotesters.PeekPokeTester
import dsptools._
import dsptools.DspTester
import dsptools.numbers._
import breeze.math.Complex

//class complex_reciprocal_io[ T <: DspComplex[FixedPoint]]( proto : T) 
class complex_reciprocal_io( w: Int, b: Int) 
   extends Bundle {
        //val N       = Input( proto)
        //val D       = Input( proto)
        //val Q       = Output( proto)
    val N= Input(DspComplex(
                FixedPoint(w.W,b.BP),
                FixedPoint(w.W,b.BP)
            ) 
        )
    val D= Input(DspComplex(
                FixedPoint(w.W,b.BP),
                FixedPoint(w.W,b.BP)
            ) 
    )
    val Q= Output(DspComplex(
                FixedPoint((2*w+2).W,(2*b).BP),
                FixedPoint((2*w+2).W,(2*b).BP)
            )
    )

        //override def cloneType = (new complex_reciprocal_io(proto)).asInstanceOf[this.type]
        override def cloneType = (new complex_reciprocal_io(w=w,b=b)).asInstanceOf[this.type]
   }

class complex_reciprocal(w: Int, b: Int) extends Module {
    //Signal type is fixed to complex fixed point
    val proto= DspComplex(
                FixedPoint(w.W,b.BP),
                FixedPoint(w.W,b.BP)
            ) 
    val proto_extend= DspComplex(
                FixedPoint((w+1).W,b.BP),
                FixedPoint((w+1).W,b.BP)
            ) 
    val proto_result= DspComplex(
                FixedPoint((2*w+2).W,(2*b).BP),
                FixedPoint((2*w+2).W,(2*b).BP)
            ) 
    //val io = IO(new complex_reciprocal_io( proto=proto.cloneType))
    val io = IO(new complex_reciprocal_io(w=w, b=b))
    val nominator= RegInit(0.U.asTypeOf(proto_result.cloneType))
    val denominator= RegInit(0.0.F((2*w+1).W,(2*b).BP))
    println(denominator.binaryPoint)
    println(denominator.getWidth)
    val D_extend=Wire(proto_extend.cloneType)
    val Dconj_extend=Wire(proto_extend.cloneType)
    val N_extend=Wire(proto_extend.cloneType)
    N_extend:=io.N
    D_extend:=io.D
    Dconj_extend.real:=D_extend.real
    Dconj_extend.imag:=0.0.F((w+1).W,b.BP)-D_extend.imag
    nominator:=N_extend*Dconj_extend
    denominator:=Dconj_extend.real*Dconj_extend.real+Dconj_extend.imag*Dconj_extend.imag
    //io.Q.real:= (nominator.real.asSInt/denominator.asSInt).asFixedPoint((2*b).BP)
    //io.Q.imag:= (nominator.imag.asSInt/denominator.asSInt).asFixedPoint((2*b).BP)
    io.Q.real:= (nominator.real.asSInt/denominator.asSInt << w).asFixedPoint((2*b).BP)
    io.Q.imag:= (nominator.imag.asSInt/denominator.asSInt << w ).asFixedPoint((2*b).BP)
}

//This gives you verilog
object complex_reciprocal extends App {
    chisel3.Driver.execute(
        args, () => new complex_reciprocal(
            w=16, b=8
        ) 
    )
}
//clasUnitmmy extends Module { val io = IO(new Bundle {}) }
class unit_tester(c: complex_reciprocal) extends DspTester(c) {
//Tests are here 
    poke(c.io.N.real, 5)
    poke(c.io.N.imag, 5)
    poke(c.io.D.real, 2)
    poke(c.io.D.imag, -2)
    step(5)
    expect(c.io.Q.real, 0)
    expect(c.io.Q.imag, 2)
}

object unit_test extends App {
    iotesters.Driver.execute(args, () => new complex_reciprocal(
            w=16, b=8
        ) 
        ){
            c=>new unit_tester(c)
    }
}

