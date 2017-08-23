package com.flybotix.hfr.io;

/**
 * Contains a list of 30 or fewer messages which are meant for the protocols themselves.
 * 
 * That number is 30 since 31 is a prime number commonly used for hashing algorithms that
 * may become message id's.
 */
public enum EStaticMessageIds {
  NULL_MESSAGE,
  BATCHED_MESSAGE
}
