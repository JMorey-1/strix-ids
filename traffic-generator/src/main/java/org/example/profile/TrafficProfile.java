package org.example.profile;

/**
 * Represents one traffic behaviour profile for the generator.
 *
 * <p>Each implementation simulates a different type of client, such as a normal user, brute-force
 * attacker, endpoint scanner or admin prober.
 */
public interface TrafficProfile {

  void run(String ipAddress) throws Exception;
}
