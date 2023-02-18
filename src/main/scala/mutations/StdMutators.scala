package work.lithos
package mutations

object StdMutators {
  def newBox(contract: Contract, amount: Long, outputIdx: Int): Mutator = {
    val output = UTXO(
      contract,
      amount
    )
    (tCtx: TxContext) => tCtx.replaceOutput(output, outputIdx)

  }
}
