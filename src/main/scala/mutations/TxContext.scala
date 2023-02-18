package work.lithos
package mutations

import org.ergoplatform.appkit.BlockchainContext

case class TxContext(ctx: BlockchainContext, inputs: Seq[InputUTXO], dataInputs: Seq[InputUTXO],
                    outputs: Seq[UTXO] = Seq.empty[UTXO]){
  def withNewOutputs(modOut: Seq[UTXO]): TxContext = this.copy(outputs = modOut)

  def addOutputs(utxos: UTXO*): Seq[UTXO] = {
    outputs++utxos
  }
  /**
   * Replaces single element in outputs with a new UTXO
   * @return New Output sequence with Output at given index replaced with a mutated output
   */
  def replaceOutput(mutated: UTXO, idx: Int): Seq[UTXO] = {
    outputs.patch(idx, Seq(mutated), 1)
  }

  def replaceOutputs(mutated: Seq[UTXO], indices: Seq[Int]): Seq[UTXO] = {
    mutated.zip(indices).foldLeft(outputs){
      (z: Seq[UTXO], c: (UTXO, Int)) => z.patch(c._2, Seq(c._1), 1)
    }
  }

}

object TxContext {

}
