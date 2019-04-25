
// Dsp-block complex_residual
// Description here 
// Inititally written by dsp-blocks initmodule.sh, 20190425
package complex_residual

import chisel3.experimental._
import chisel3._
import dsptools._
import dsptools.numbers._
import breeze.math.Complex

class complex_residual_io[T <:Data](proto: T,n: Int) 
   extends Bundle {
        val A       = Input(Vec(n,proto))
        val B       = Output(Vec(n,proto))
        override def cloneType = (new complex_residual_io(proto.cloneType,n)).asInstanceOf[this.type]
   }

class complex_residual[T <:Data] (proto: T,n: Int) extends Module {
    val io = IO(new complex_residual_io( proto=proto, n=n))
    val register=RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(proto.cloneType))))
    register:=io.A
    io.B:=register
}

//This gives you verilog
object complex_residual extends App {
    chisel3.Driver.execute(args, () => new complex_residual(
        proto=DspComplex(UInt(16.W),UInt(16.W)), n=8) 
    )
}
