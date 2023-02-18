package work.lithos
package collateral

import org.ergoplatform.appkit.scalaapi.ErgoValueBuilder
import org.ergoplatform.appkit.{Address, ErgoId, ErgoValue, NetworkType, Parameters, PreHeader, RestApiErgoClient}
import org.scalatest.funsuite.AnyFunSuite
import sigmastate.eval.CostingSigmaDslBuilder.Colls
import work.lithos.lfsm.collateral.CollateralContract
import work.lithos.mutations.{Contract, Token, TxBuilder, UTXO}
import work.lithos.utils.ScriptGenerator

import java.math.BigInteger

class CollateralSuite extends AnyFunSuite{

  val networkType: NetworkType = NetworkType.TESTNET
  val nodePort: String = if (networkType == NetworkType.MAINNET) ":9053/" else ":9052/"
  val emissionId: ErgoId = ErgoId.create("20fa2bf23962cdf51b07722d6237c0c7b8a44f78856c0f7ec308dc1ef1a92a51")
  val client = RestApiErgoClient.create(
    "http://213.239.193.208" + nodePort,
    networkType,
    "",
    RestApiErgoClient.getDefaultExplorerUrl(networkType))


  test("Pay Collateral No Change"){
    client.execute{
      ctx =>
        val collateral: Contract = CollateralContract.mkCollatContract(ctx, emissionId)
        val feeValue: Long = Parameters.MinFee * 100
        val lender   = Address.create("9exChj86cfLEV1A3fuGYo4ciBVgHaDHU1UnyHrdBvvtVYFUgUxn")
        val lenderPk = lender.getPublicKey
        val currentBlockReward = CollateralContract.coinsToIssue(ctx.getHeight)
        val collateralUTXO = UTXO(
          collateral,
          currentBlockReward - feeValue,
          registers = Seq(
            ErgoValueBuilder.buildFor(feeValue),
            ErgoValue.of(lenderPk)
          )
        ).toDummyInput(ctx)

        val fakeEmissionUTXO = UTXO(
          Contract.fromAddressString("2Z4YBkDsDvQj8BX7xiySFewjitqp2ge9c99jfes2whbtKitZTxdBYqbrVZUvZvKv6aqn9by4kp3LE1c26LCyosFnVnm6b6U1JYvWpYmL2ZnixJbXLjWAWuBThV1D6dLpqZJYQHYDznJCk49g5TUiS4q8khpag2aNmHwREV7JSsypHdHLgJT7MGaw51aJfNubyzSKxZ4AJXFS27EfXwyCLzW1K6GVqwkJtCoPvrcLqmqwacAWJPkmh78nke9H4oT88XmSbRt2n9aWZjosiZCafZ4osUDxmZcc5QVEeTWn8drSraY3eFKe8Mu9MSCcVU"),
          1000000L * Parameters.OneErg,
          tokens = Seq(Token(emissionId, 1)),
          registers = Seq(ErgoValueBuilder.buildFor(Colls.fromArray(collateralUTXO.id.getBytes)))
        ).toDummyInput(ctx)

        val sigTrue = UTXO(
          ScriptGenerator.mkSigTrue(ctx),
          currentBlockReward - feeValue - Parameters.MinFee,
          registers = Seq(ErgoValueBuilder.buildFor(Colls.fromArray(collateralUTXO.id.getBytes)))
        )

        val txB = TxBuilder(ctx)
        val uTx = txB
          .setInputs(collateralUTXO)
          .setDataInputs(fakeEmissionUTXO)
          .setOutputs(sigTrue)
          .setPreHeader(ctx.createPreHeader().minerPk(lender.getPublicKeyGE).build())
          .buildTx(Parameters.MinFee, lender)

        val dummyProver = ctx.newProverBuilder().withDLogSecret(BigInteger.ZERO).build()
        println("CoinsToIssue: " + currentBlockReward)
        val sTx = dummyProver.sign(uTx)
        println(sTx.toJson(true, true))
    }

  }

  test("Pay Collateral With Change"){
    client.execute{
      ctx =>
        val collateral: Contract = CollateralContract.mkCollatContract(ctx, emissionId)
        val feeValue: Long = Parameters.MinFee * 100
        val lender   = Address.create("9exChj86cfLEV1A3fuGYo4ciBVgHaDHU1UnyHrdBvvtVYFUgUxn")
        val lenderPk = lender.getPublicKey
        val currentBlockReward = CollateralContract.coinsToIssue(ctx.getHeight)
        val collateralUTXO = UTXO(
          collateral,
          currentBlockReward + Parameters.OneErg * 5,
          registers = Seq(
            ErgoValueBuilder.buildFor(feeValue),
            ErgoValue.of(lenderPk)
          )
        ).toDummyInput(ctx)

        val fakeEmissionUTXO = UTXO(
          Contract.fromAddressString("2Z4YBkDsDvQj8BX7xiySFewjitqp2ge9c99jfes2whbtKitZTxdBYqbrVZUvZvKv6aqn9by4kp3LE1c26LCyosFnVnm6b6U1JYvWpYmL2ZnixJbXLjWAWuBThV1D6dLpqZJYQHYDznJCk49g5TUiS4q8khpag2aNmHwREV7JSsypHdHLgJT7MGaw51aJfNubyzSKxZ4AJXFS27EfXwyCLzW1K6GVqwkJtCoPvrcLqmqwacAWJPkmh78nke9H4oT88XmSbRt2n9aWZjosiZCafZ4osUDxmZcc5QVEeTWn8drSraY3eFKe8Mu9MSCcVU"),
          1000000L * Parameters.OneErg,
          tokens = Seq(Token(emissionId, 1)),
          registers = Seq(ErgoValueBuilder.buildFor(Colls.fromArray(collateralUTXO.id.getBytes)))
        ).toDummyInput(ctx)

        val sigTrue = UTXO(
          ScriptGenerator.mkSigTrue(ctx),
          currentBlockReward - feeValue - Parameters.MinFee,
          registers = Seq(ErgoValueBuilder.buildFor(Colls.fromArray(collateralUTXO.id.getBytes)))
        )

        val changeBox = UTXO(
          Contract.fromAddress(lender),
          Parameters.OneErg * 5 + feeValue
        )

        val txB = TxBuilder(ctx)
        val uTx = txB
          .setInputs(collateralUTXO)
          .setDataInputs(fakeEmissionUTXO)
          .setOutputs(sigTrue, changeBox)
          .setPreHeader(ctx.createPreHeader().minerPk(lender.getPublicKeyGE).build())
          .buildTx(Parameters.MinFee, lender)

        val dummyProver = ctx.newProverBuilder().withDLogSecret(BigInteger.ZERO).build()
        println("CoinsToIssue: " + currentBlockReward)
        val sTx = dummyProver.sign(uTx)
        println(sTx.toJson(true, true))
    }

  }
}
