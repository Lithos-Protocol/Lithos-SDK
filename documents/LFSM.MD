# Lithos Finite State Machine
The Lithos Finite State Machine (LFSM) is the main set of contracts and off-chain code which make up all possible
interactions with the finite state machine managing the Lithos protocol. The LFSM contains the following
components:
- NISP Optimistic Rollup
- Fraud Proof Interactions
- Lithos Token Emissions
- Global *s-diff* Database
- Payment Claiming
- Collateral Selection

The LFSM represents the core of the Lithos protocol. Each local instance of the reference client interacts with the LFSM
in order to allow for the following interactions:
- Submissions of new blocks to the rollup
- NISP submissions to the rollup
- Optimistic verification of NISPs
- Off-chain evaluation of NISPs
- Fraud Proof submissions
- Claiming of *s-diff*
- Emission of Lithos tokens
- Selection and usage of collateral contracts
- Payments to miners