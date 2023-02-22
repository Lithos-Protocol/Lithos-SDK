# ErgoHack VI - Collateralized Mining Pools

As a part of ErgoHack VI, the Lithos team decided to pursue work towards the implementation
of collateralized mining pools, a key part of the protocol. Our goals were simple, we wished
to create the contracts needed to support collateralization by third parties, in a way which allowed
for miners to select their collateral, while at the same time supporting novel concepts in PoW such as
PoW-DeFi and Fair Initial Mining Offerings (FIMOs). In this document, we wish to go over the history and importance of 
collateralized mining pools, our implementation of a collateral contract, and our live testing of transactions.

## History
The concept of collateralized mining pools begins with Ergo's initial implementation of Autolykos, which
involved a non-outsourceable PoW scheme. Non-Outsourceable PoW schemes are intended to prevent pooled mining,
which serves as a point of centralization in all standard PoW blockchains today. Autolykos I therefore allowed
for extremely diverse block production, due to the fact that all miners on Ergo were solo miners, who
created their blocks locally. Eventually, further [research](https://eprint.iacr.org/2020/044.pdf) showed that
Non-Outsourceability could be broken by blockchains which supported smart contracts (like Ergo),
through the use of collateralized smart contracts.

The general concept worked like this: Each miner in a collateralized mining pool would reserve enough ERG to
pay out the block reward. Each miner would mine to their own node and receive the standard block reward upon
mining a block. However, once a block was mined, the collateral set aside by the miner would go to the pool operator.
This allowed for the pool operator to collect shares and eventually pay out all other members of the pool accordingly.

Due to the unfair advantage that members of such a mining pool could have, Autolykos II was released, which allowed
for standard mining pools to operate on Ergo. It should be noted that in terms of decentralization, collateralized
mining pools are still advantageous over standard ones, in that they are far more censorship resistance. This is
evident by the fact that collateralized pools still allow for local and varied block production, something not supported
by standard pools.

## Lithos
The Lithos Protocol is a layer-2 which wishes to create fully decentralized mining pools. As part of ErgoHack V,
we created and showed off our concept of [Non-Interactive Share Proofs (NISPs)](https://www.youtube.com/watch?v=x-9x_PveRog), which allowed for evaluation of
shares on the blockchain. This serves as just one half of the protocol. While NISPs allow for payments and share
evaluation to be decentralized, the issue of block production itself cannot be solved by them.

We turn to collateralized mining pools (CMPs) as a way to decentralize block production, even within an outsourceable PoW
setting. One important reason for their usage is that Ergo's emission contract requires paying out to a miner
with some public key. Collateral contracts can be made to have no such rules involved, meaning that payment
could instead be sent to some contract which then evaluates NISPs. Moreover, CMPs allow for block production
similar to that of Autolykos I, due to all blocks being produced locally by miners. This increases censorship
resistance, gives more direct voting power to miners, and gives miners the chance to participate in MEV
through transaction selection. Finally, CMPs can inherently support DeFi and FIMOs, because the collateral provided need
not be entirely made of ERG. Third party collateralizers earning fees can also be used to help ensure that miners have no
requirements for owning their own ERG while participating in the decentralized pool.

## Contract Implementation

The goal of our collateral contract was to allow for third party collateralization, the ability to gain fees,
the prevention of abuse by self collateralizers, and the insurance that only one collateral box could be spent
per block. Our main contract can be viewed [here](/src/main/resources/contracts/collateral/Collateral_Mainnet.ergo)

The general idea of this contract is to calculate the amount of ERG to be emitted at the current height, and simply
ensure the collateral box can cover it. Once verified the collateral is paid out to the pool contract (currently a filler contract), with the fee taken
out of the payment. While the contract is free to view, we wish to point out a few key points about it which makes it
rather unique and unlike standard contracts.

### Lender & Block Miner
In the collateral contracts made during Autolykos I, the miner self-collateralized. Although this is still allowed,
within our own contracts, we wished to focus on the ability for third parties to collateralize pools. By leveraging
the fact that Autolykos II is an outsourceable PoW scheme, we added the requirement of `lenderPk == CONTEXT.preHeader.minerPk`.
In short, this means that miners will mine under the public key associated with the lender whose collateral box
the miner wishes to use. This allows for the lender to be paid back using the standard block reward, while their collateral
is used to pay out the pool.

### Coinbase Modification
One interesting point of the contract is that it interacts with Ergo's standard emission contract. To be exact,
the miner of the block must do two things in order to ensure that the contract executes successfully. When
making the coinbase transaction, the miner must add the box id of the collateral box they wish to use onto
R4 of the newly outputted emission box. Although this is a non-standard way of creating the coinbase transaction,
no rules on the emission contract prevent this. An example of such a coinbase transaction on testnet can be
viewed [here](https://tn-ergo-explorer.anetabtc.io/en/transactions/5ed9cbf2f15c4fb26e8be2ac6dadb0749ec864545b60262bc6085dc84fda815a)

After setting R4 on the outputted emission box, the miner then includes the new emission box into the collateral transaction
as a data input. The collateral contract then verifies that its id is equal to the id set on the new emission box. This has the important affect
of making sure that only one collateral box can be spent per block. Allowing multiple collateral transactions per
block would mean that some lenders could not be paid back, as there is only one block reward per block.

## Contract Files
We created multiple contracts to help aid us during tests. The main contract is called
`Collateral_Mainnet.ergo` visible [here](/src/main/resources/contracts/collateral/Collateral_Mainnet.ergo)

Our testnet contract is slightly modified from this one. It does not check for the Emission NFT, which
does not exist in testnet due to the lack of EIP-27 activation there.

The `Collateral_Test.ergo` contract was used for offline tests. It implements a tx fee, which is required
to perform offline tests using Ergo-Appkit. In reality, the collateral transaction would not require a transaction
fee due to being directly inserted into the block by the miner.

The `Collateral_Testnet_Solo.ergo` contract was used to play around with the idea of FIMOs for Solo miners.
Instead of paying to a pool, the contract does not check which address the collateral is paid to. This would
allow Solo miners to gain the contents of the collateral box, which in this contract also transfers over all assets.

## Implementation

We modified an Ergo node specifically to perform a live test of a collateral transaction. Unfortunately,
the current Ergo node does not have API calls to enable modification to the coinbase transaction, nor
the ability to use its emission box as a data input. For this reason, changes had to be hard-coded directly
into the node during candidate generation. A link to the modified node repo can be found [here](https://github.com/Lithos-Protocol/Lithos-ErgoNode/tree/Lithos-Tx-Test)

An example of a live collateral transaction can be viewed in [this block on Ergo's testnet](https://tn-ergo-explorer.anetabtc.io/en/blocks/a79a093a8fe6070de87f414b5e0f9b880d057b7adbfc7384c75a6eb43b95a52a)
As can be seen from the block, the coinbase transaction was successfully modified to have the collateralId on R4 of the new emission box.
This collateral box was then used in the following transaction to pay out to the filler pool contract. The lack
of transaction fees shows that the transaction was directly inserted into the block by the miner. The contract
used for this test was `Collateral_Testnet.ergo`

# UI
One of our goals was to implement a demo UI during the hackathon. Unfortunately, we were unable to complete
the demo UI within the timespan of the hackathon. We were able to get the initial collateral transaction
working, along with styling of the website. The UI demo is heavily based off of *Captain Nemo's* SigmaFi website.
A link to the repo is visible [here](https://github.com/Lithos-Protocol/Front-End)

![image](/documents/LithosFrontEnd.PNG)
