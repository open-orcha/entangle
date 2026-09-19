// The content contract shared by the iOS, Android, and web apps.
// Edit ../../content/lessons.json, never the copies the apps render.
import data from "../../content/lessons.json";

export interface Lesson {
  id: string;
  title: string;
  summary: string;
  body: string;
}

export interface QuizQuestion {
  id: string;
  lessonId: string;
  question: string;
  choices: string[];
  answerIndex: number;
  explanation: string;
}

export interface CoinFlipCopy {
  title: string;
  intro: string;
  entangledLabel: string;
  classicalLabel: string;
  measureLabel: string;
  resetLabel: string;
  disclaimer: string;
}

export interface Content {
  app: { name: string; tagline: string; contentVersion: number };
  lessons: Lesson[];
  quiz: QuizQuestion[];
  coinFlip: CoinFlipCopy;
}

export const content: Content = data;

export function lessonById(id: string): Lesson | undefined {
  return content.lessons.find((l) => l.id === id);
}
