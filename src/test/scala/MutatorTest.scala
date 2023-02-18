package work.lithos

import mutations._

import org.ergoplatform.appkit._
import org.scalatest.funsuite.AnyFunSuite

class MutatorTest extends AnyFunSuite{

  val networkType: NetworkType = NetworkType.TESTNET
  val nodePort: String = if (networkType == NetworkType.MAINNET) ":9053/" else ":9052/"

  val client = RestApiErgoClient.create(
    "http://213.239.193.208" + nodePort,
    networkType,
    "",
    RestApiErgoClient.getDefaultExplorerUrl(networkType))


  test("Standard Mutator") {
    client.execute {
      ctx =>

        val sigmaTrue = ctx.compileContract(ConstantsBuilder.empty(), " { sigmaProp(true) } ")

        val txBuilder = TxBuilder(ctx)
        val prover = ctx.newProverBuilder().withDLogSecret(BigInt(0).bigInteger).build()
        val input = UTXO(
          Contract.fromErgoContract(sigmaTrue),
          Parameters.OneErg * 5
        ).toDummyInput(ctx).withMutator(
          StdMutators.newBox(
            Contract.fromAddressString("9fq4Ha1xKGpGsg8e11wD6q7fCU4BQHYb34vqEBVQwp3cgoneEHA"),
            Parameters.OneErg,
            0
          )
        )

        val uTx = txBuilder
          .setInputs(input)
          .mutateOutputs
          .buildTx(Parameters.MinFee, Address.create("9fAMzWJa91Bdgh4a9zaHbdhjmeCsJSFCb75HFnTWV7gfTF6kDEs"))
        val sTx = prover.sign(uTx)

        println(sTx.toJson(true))
    }
  }

  test("Lambda Mutation"){
    client.execute {
      ctx =>

        val sigmaTrue = Contract.fromErgoScript(ctx, ConstantsBuilder.empty(), " { sigmaProp(true) } ")

        val txBuilder = TxBuilder(ctx)
        val prover = ctx.newProverBuilder().withDLogSecret(BigInt(0).bigInteger).build()
        val utxo = UTXO(
          sigmaTrue,
          Parameters.OneErg * 5,
          tokens = Seq(Token("6e6547eb720ac46703d20a2903fc588c9a7079d2f32897b6f222cf443c5cdac7", 100))
        )

        val mutator: Mutator = {
          (tCtx: TxContext) =>
            tCtx.addOutputs(tCtx.inputs.head.toUTXO
              .removeToken(Token("6e6547eb720ac46703d20a2903fc588c9a7079d2f32897b6f222cf443c5cdac7", 50))
              .subValue(Parameters.MinFee)
            )
        }
            val input = utxo.toDummyInput(ctx).withMutator{
              mutator
            }

            val uTx = txBuilder
              .setInputs(input)
              .mutateOutputs
              .buildTx(
                Parameters.MinFee,
                Address.create("9fAMzWJa91Bdgh4a9zaHbdhjmeCsJSFCb75HFnTWV7gfTF6kDEs"),
                Seq(Token("6e6547eb720ac46703d20a2903fc588c9a7079d2f32897b6f222cf443c5cdac7", 50))
              )
            val sTx = prover.sign(uTx)

            println(sTx.toJson(true))
        }
    }
  }

