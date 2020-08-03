package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve{
  
  /**
   * {@inheritDoc}
   * TODO Use the SieveActorActor class to calculate the number of primes <=
   * limit in parallel. You might consider how you can model the Sieve of
   * Eratosthenes as a pipeline of actors, each corresponding to a single
   * prime number.
   */
  @Override
  public int countPrimes( final int limit ){
   
    final SieveActorActor sieveActor = new SieveActorActor( 2 );
    PCDP.finish(()->{
      for( int i = 3; i <= limit; i+=2 ){
        sieveActor.send( i );
      }
      sieveActor.send( 0 );
    });
    
    
    int numPrimes = 0;
    SieveActorActor loops= sieveActor;
    while( loops!=null ){
      numPrimes+=loops.numLocalPrimes();
      loops=loops.nextActor();
    }
    
    
    return numPrimes;
  }
  
  /**
   * An actor class that helps implement the Sieve of Eratosthenes in
   * parallel.
   */
  public static final class SieveActorActor extends Actor{
    
    /**
     * Process a single message sent to this actor.
     * TODO complete this method.
     * @param msg
     * Received message
     */
    private static final int MAX_LOCAL_PRIMES = 150;
    private final int        localPrimes[];
    private int              numLocalPrimes;
    SieveActorActor          nexActor;
    
    public SieveActorActor( final int localPrime ){
      this.localPrimes = new int[MAX_LOCAL_PRIMES];
      this.localPrimes[0] = localPrime;
      this.numLocalPrimes = 1;
      this.nexActor = null;
    }
    
    public SieveActorActor nextActor(){
      return nexActor;
    }
    
    public int numLocalPrimes(){
      return numLocalPrimes;
    }
    
    @Override
    public void process( final Object msg ){
      final int candidate = (Integer)msg;
      if( candidate <= 0 ){
        if( nexActor != null ){
          nexActor.send( msg );
        }
      }
      else{
        final boolean locallyPrime = isLocallyPrime( candidate );
        if( locallyPrime ){
          if( numLocalPrimes < MAX_LOCAL_PRIMES ){
            localPrimes[numLocalPrimes] = candidate;
            numLocalPrimes += 1;
          }
          else if( nexActor == null ){
            nexActor = new SieveActorActor( candidate );
          }
          else{
            nexActor.send( msg );
          }
        }
      }
    }
    
    private boolean isLocallyPrime( int candidate ){
      final boolean[] isPrime = { true };
      checkPrimeKernel( candidate, isPrime, 0, numLocalPrimes );
      return isPrime[0];
    }
    
    private void checkPrimeKernel( int candidate, boolean[] isPrime, int startIndex, int endIndex ){
      for( int j = startIndex; j < endIndex; j++ ){
        if( candidate % localPrimes[j] == 0 ){
          isPrime[0] = false;
          break;
        }
      }
    }
  }
}
