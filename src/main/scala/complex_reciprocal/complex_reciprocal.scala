// Dsp-block complex_reciprocal
// Description here 
// Inititally written by dsp-blocks initmodule.sh, 20190425
package complex_reciprocal

import chisel3.experimental._
import chisel3._
//import chisel3.iotesters.PeekPokeTester
import dsptools._
import dsptools.{DspTester, DspTesterOptionsManager, DspTesterOptions}
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

    //val io = IO(new complex_reciprocal_io( proto=proto.cloneType))
    val io = IO(new complex_reciprocal_io(w=w, b=b))
    val proto_extend= DspComplex(
                FixedPoint((io.N.real.getWidth+1).W,io.N.real.binaryPoint),
                FixedPoint((io.N.imag.getWidth+1).W,io.N.imag.binaryPoint)
            ) 

    val D_extend=RegInit(0.U.asTypeOf(proto_extend.cloneType))
    val N_extend=RegInit(0.U.asTypeOf(proto_extend.cloneType))

    val nominator= RegInit(0.U.asTypeOf(io.Q.cloneType))
    val denominator= RegInit(0.U.asTypeOf(io.Q.real))

    N_extend:=io.N
    D_extend:=io.D

    nominator:=N_extend*D_extend.conj()
    denominator:=D_extend.conj().abssq()

    //Scale makes these integers
    //Q.binaryPoint gives you decimals
    val scale=Seq(io.N.real.binaryPoint.get,denominator.binaryPoint.get).max

    io.Q.real:= (
        (nominator.real << (scale+io.Q.real.binaryPoint.get)).asSInt
        / (denominator << io.Q.real.binaryPoint.get).asSInt
    ).asFixedPoint(io.Q.real.binaryPoint)
    io.Q.imag:= (
        (nominator.imag << (scale+io.Q.imag.binaryPoint.get)).asSInt
        / (denominator << io.Q.imag.binaryPoint.get).asSInt
    ).asFixedPoint(io.Q.imag.binaryPoint)
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
    val N=Complex(1.0,1.0)
    val D=Complex(6.0,0.0)
    poke(c.io.N, N)
    poke(c.io.D, D)
    step(5)
    expect(c.io.Q, N/D)
}

object unit_test extends App {
    val testOptions = new DspTesterOptionsManager {
        dspTesterOptions = DspTesterOptions(
        fixTolLSBs = 1
    )
    }
    iotesters.Driver.execute(args, () => new complex_reciprocal(
            w=16, b=8
        ) 
        ){
            c=>new unit_tester(c)
    }
}

