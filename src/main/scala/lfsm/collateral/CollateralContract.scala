package work.lithos
package lfsm.collateral

import mutations.Contract

import org.ergoplatform.appkit.{BlockchainContext, ConstantsBuilder, ErgoId}
import sigmastate.eval.CostingSigmaDslBuilder.Colls
import work.lithos.utils.ScriptGenerator

object CollateralContract {
  final val FIXED_RATE_PERIOD = 525600L
  final val FIXED_RATE        = 75000000000L
  final val FOUND_INIT_RWRD   = 7500000000L
  final val EPOCH_LENGTH      = 64800L
  final val ONE_EPOCH_RED     = 3000000000L

  def mkCollatContract(ctx: BlockchainContext, emissionId: ErgoId): Contract = {
    val constants = ConstantsBuilder
      .create()
      .item("_EMISSION_ID", Colls.fromArray(emissionId.getBytes))
      .item("_ROLLUP_HOLDING_HASH", Colls.fromArray(ScriptGenerator.mkSigTrue(ctx).hashedPropBytes))
      .item("_FIXED_RATE", FIXED_RATE)
      .item("_FIXED_RATE_PERIOD", FIXED_RATE_PERIOD)
      .item("_FOUNDER_INIT_REWARD", FOUND_INIT_RWRD)
      .item("_EPOCH_LENGTH", EPOCH_LENGTH)
      .item("_ONE_EPOCH_REDUCTION", ONE_EPOCH_RED)
      .build()

    Contract.fromErgoScript(ctx, constants, ScriptGenerator.mkCollatScript("Collateral"))
  }

  def coinsToIssue(height: Long): Long = {
    val minersReward = FIXED_RATE - FOUND_INIT_RWRD
    val minersFixedRatePeriod = FIXED_RATE_PERIOD + 2 * EPOCH_LENGTH
    val epoch = 1 + (height - FIXED_RATE_PERIOD) / EPOCH_LENGTH

    if(height < minersFixedRatePeriod){
      minersReward
    }else{
      FIXED_RATE - (ONE_EPOCH_RED * epoch)
    }
  }
}
