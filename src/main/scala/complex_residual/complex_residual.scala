
// Dsp-block complex_residual
// Description here 
// Inititally written by dsp-blocks initmodule.sh, 20190425
package complex_residual

import chisel3.experimental._
import chisel3._
import chisel3.iotesters.PeekPokeTester
import dsptools._
import dsptools.numbers._
import breeze.math.Complex

class complex_residual_io[ T <: DspComplex[FixedPoint]]( proto : T) 
   extends Bundle {
        val N       = Input( proto)
        val D       = Input( proto)
        val Q       = Output( proto)
        override def cloneType = (new complex_residual_io(proto)).asInstanceOf[this.type]
   }

class complex_residual(w: Int, b: Int) extends Module {
    //Signal type is fixed to complex fixed point
    val proto= DspComplex(
                FixedPoint(w.W,b.BP),
                FixedPoint(w.W,b.BP)
            ) 
    val io = IO(new complex_residual_io( proto=proto.cloneType))
    val nominator= RegInit(0.U.asTypeOf(proto.cloneType))
    val denominator= RegInit(0.0.F((w+1).W,b.BP))
    val Dconj=Wire(proto.cloneType)
    Dconj.real:=io.D.real
    Dconj.imag:=0.0.F(w.W,b.BP)-io.D.imag
    nominator:=io.N*Dconj
    denominator:=io.D.real*io.D.real+io.D.imag*io.D.imag
    //io.Q.real:= (nominator.real.asSInt/denominator.asSInt).asFixedPoint(b.BP)
    //io.Q.imag:= (nominator.imag.asSInt/denominator.asSInt).asFixedPoint(b.BP)
    io.Q.real:= nominator.real 
    io.Q.imag:= nominator.imag 

}

//This gives you verilog
object complex_residual extends App {
    chisel3.Driver.execute(
        args, () => new complex_residual(
            w=16, b=8
        ) 
    )
}
//class Dummy extends Module { val io = IO(new Bundle {}) }
class DummyTester(c: complex_residual) extends PeekPokeTester(c) {
    poke(c.io.N.real, 5)
    poke(c.io.N.imag, 5)
    step(5)
    //println(peek(c.io.Q.real).toString)
//tests are here I guess
}

//object Dummy extends App { iotesters.Driver.execute(args, () => new Dummy){ c => new DummyTester(c) }}

object complex_residual_test extends App {
    iotesters.Driver.execute(args, () => new complex_residual(
            w=16, b=8
        ) 
        ){
            c=>new DummyTester(c)
    }
}

