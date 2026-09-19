// Toy model. See content/lessons.json -> coinFlip.disclaimer.
export type Mode = "entangled" | "classical";
export type Face = "H" | "T";

export interface FlipResult {
  a: Face;
  b: Face;
}

export interface Tally {
  flips: number;
  agreements: number;
}

const face = (r: number): Face => (r < 0.5 ? "H" : "T");

/** One measurement of the pair. `random` is injectable for tests. */
export function flipPair(mode: Mode, random: () => number = Math.random): FlipResult {
  if (mode === "entangled") {
    // One shared random outcome: the two coins always agree.
    const shared = face(random());
    return { a: shared, b: shared };
  }
  // Two independent flips: they agree about half the time.
  return { a: face(random()), b: face(random()) };
}

export function record(tally: Tally, result: FlipResult): Tally {
  return {
    flips: tally.flips + 1,
    agreements: tally.agreements + (result.a === result.b ? 1 : 0),
  };
}

export function agreementRate(tally: Tally): number {
  return tally.flips === 0 ? 0 : tally.agreements / tally.flips;
}
