import { describe, expect, it } from "vitest";
import { content } from "./content";
import { agreementRate, flipPair, record } from "./coinflip";

describe("content contract (content/lessons.json)", () => {
  it("has five lessons with unique ids", () => {
    expect(content.lessons).toHaveLength(5);
    const ids = new Set(content.lessons.map((l) => l.id));
    expect(ids.size).toBe(content.lessons.length);
  });

  it("has five quiz questions that each point at a real lesson and a valid answer", () => {
    expect(content.quiz).toHaveLength(5);
    const lessonIds = new Set(content.lessons.map((l) => l.id));
    for (const q of content.quiz) {
      expect(lessonIds.has(q.lessonId)).toBe(true);
      expect(q.choices.length).toBeGreaterThanOrEqual(2);
      expect(q.answerIndex).toBeGreaterThanOrEqual(0);
      expect(q.answerIndex).toBeLessThan(q.choices.length);
    }
  });
});

describe("entangled coin flip", () => {
  it("entangled mode always agrees", () => {
    for (let i = 0; i < 200; i++) {
      const r = flipPair("entangled");
      expect(r.a).toBe(r.b);
    }
  });

  it("classical mode uses two independent draws", () => {
    const draws = [0.1, 0.9];
    let n = 0;
    const r = flipPair("classical", () => draws[n++]);
    expect(r).toEqual({ a: "H", b: "T" });
  });

  it("tally tracks agreement rate", () => {
    let t = { flips: 0, agreements: 0 };
    t = record(t, { a: "H", b: "H" });
    t = record(t, { a: "H", b: "T" });
    expect(t).toEqual({ flips: 2, agreements: 1 });
    expect(agreementRate(t)).toBe(0.5);
  });
});
