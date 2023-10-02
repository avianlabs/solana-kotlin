package net.avianlabs.solana.domain.core


/**
 * [Commitment.Finalized] - the node will query the most recent block confirmed by supermajority of the cluster as having reached maximum lockout, meaning the cluster has recognized this block as finalized
 * [Commitment.Confirmed] - the node will query the most recent block that has been voted on by supermajority of the cluster.
 * It incorporates votes from gossip and replay.
 * It does not count votes on descendants of a block, only direct votes on that block.
 * This confirmation level also upholds "optimistic confirmation" guarantees in release 1.3 and onwards.
 * [Commitment.Processed] - the node will query its most recent block. Note that the block may still be skipped by the cluster.
 *
 * @see <a href="https://docs.solana.com/api/http#configuring-state-commitment">Configuring state commitment</a>
 */
public enum class Commitment(public val value: String) {
  Finalized("finalized"),
  Confirmed("confirmed"),
  Processed("processed"),
}
