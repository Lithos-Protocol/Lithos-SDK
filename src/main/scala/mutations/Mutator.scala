package work.lithos
package mutations

trait Mutator {
  def mutate(tCtx: TxContext): Seq[UTXO]
}
