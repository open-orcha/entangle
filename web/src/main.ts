import "./style.css";
import { content, lessonById } from "./content";
import { agreementRate, flipPair, record, type FlipResult, type Mode, type Tally } from "./coinflip";

const app = document.querySelector<HTMLDivElement>("#app")!;

// ---------- tiny helpers ----------

function esc(s: string): string {
  return s.replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]!);
}

function paragraphs(body: string): string {
  return body
    .split("\n\n")
    .map((p) => `<p>${esc(p)}</p>`)
    .join("");
}

function shell(title: string, inner: string, back?: { href: string; label: string }): string {
  const nav = back ? `<a class="back" href="${back.href}">&larr; ${esc(back.label)}</a>` : "";
  return `
    <header>
      ${nav}
      <h1>${esc(title)}</h1>
    </header>
    <main>${inner}</main>
    <footer>
      <a href="#/">Lessons</a> &middot; <a href="#/quiz">Quiz</a> &middot; <a href="#/coin">${esc(content.coinFlip.title)}</a>
    </footer>`;
}

// ---------- views ----------

function lessonsView(): void {
  const items = content.lessons
    .map(
      (l, i) => `
      <li><a class="card" href="#/lesson/${l.id}">
        <span class="num">${i + 1}</span>
        <span><strong>${esc(l.title)}</strong><br /><small>${esc(l.summary)}</small></span>
      </a></li>`,
    )
    .join("");
  app.innerHTML = shell(
    content.app.name,
    `<p class="tagline">${esc(content.app.tagline)}</p>
     <ol class="lessons">${items}</ol>
     <div class="actions">
       <a class="button" href="#/quiz">Take the quiz</a>
       <a class="button secondary" href="#/coin">Try the ${esc(content.coinFlip.title.toLowerCase())}</a>
     </div>`,
  );
}

function lessonView(id: string): void {
  const lesson = lessonById(id);
  if (!lesson) {
    location.hash = "#/";
    return;
  }
  const idx = content.lessons.indexOf(lesson);
  const next = content.lessons[idx + 1];
  const nextLink = next
    ? `<a class="button" href="#/lesson/${next.id}">Next: ${esc(next.title)}</a>`
    : `<a class="button" href="#/quiz">Take the quiz</a>`;
  app.innerHTML = shell(
    lesson.title,
    `<p class="tagline">${esc(lesson.summary)}</p>
     <article>${paragraphs(lesson.body)}</article>
     <div class="actions">${nextLink}</div>`,
    { href: "#/", label: "Lessons" },
  );
}

interface QuizState {
  index: number;
  selected: number | null;
  score: number;
}

const quiz: QuizState = { index: 0, selected: null, score: 0 };

function quizView(): void {
  const questions = content.quiz;
  if (quiz.index >= questions.length) {
    app.innerHTML = shell(
      "Quiz complete",
      `<p class="score">You scored ${quiz.score} out of ${questions.length}.</p>
       <div class="actions">
         <button class="button" id="again">Try again</button>
         <a class="button secondary" href="#/">Back to lessons</a>
       </div>`,
      { href: "#/", label: "Lessons" },
    );
    document.querySelector("#again")!.addEventListener("click", () => {
      Object.assign(quiz, { index: 0, selected: null, score: 0 });
      quizView();
    });
    return;
  }

  const q = questions[quiz.index];
  const answered = quiz.selected !== null;
  const choices = q.choices
    .map((c, i) => {
      let cls = "choice";
      if (answered && i === q.answerIndex) cls += " correct";
      if (answered && i === quiz.selected && i !== q.answerIndex) cls += " wrong";
      return `<li><button class="${cls}" data-i="${i}" ${answered ? "disabled" : ""}>${esc(c)}</button></li>`;
    })
    .join("");
  const feedback = answered
    ? `<p class="feedback">${quiz.selected === q.answerIndex ? "Correct." : "Not quite."} ${esc(q.explanation)}</p>
       <div class="actions"><button class="button" id="next">${quiz.index + 1 < questions.length ? "Next question" : "See result"}</button></div>`
    : "";

  app.innerHTML = shell(
    `Question ${quiz.index + 1} of ${questions.length}`,
    `<p class="question">${esc(q.question)}</p>
     <ul class="choices">${choices}</ul>
     ${feedback}`,
    { href: "#/", label: "Lessons" },
  );

  app.querySelectorAll<HTMLButtonElement>(".choice").forEach((b) =>
    b.addEventListener("click", () => {
      quiz.selected = Number(b.dataset.i);
      if (quiz.selected === q.answerIndex) quiz.score += 1;
      quizView();
    }),
  );
  document.querySelector("#next")?.addEventListener("click", () => {
    quiz.index += 1;
    quiz.selected = null;
    quizView();
  });
}

interface CoinState {
  mode: Mode;
  last: FlipResult | null;
  tally: Tally;
}

const coin: CoinState = { mode: "entangled", last: null, tally: { flips: 0, agreements: 0 } };

function coinView(): void {
  const c = content.coinFlip;
  const rate = Math.round(agreementRate(coin.tally) * 100);
  const faceOf = (f: "H" | "T" | undefined) => (f === undefined ? "?" : f === "H" ? "Heads" : "Tails");
  app.innerHTML = shell(
    c.title,
    `<p class="tagline">${esc(c.intro)}</p>
     <div class="modes">
       <button class="mode ${coin.mode === "entangled" ? "on" : ""}" data-mode="entangled">${esc(c.entangledLabel)}</button>
       <button class="mode ${coin.mode === "classical" ? "on" : ""}" data-mode="classical">${esc(c.classicalLabel)}</button>
     </div>
     <div class="coins">
       <div class="coin ${coin.last?.a ?? ""}">${faceOf(coin.last?.a)}</div>
       <div class="coin ${coin.last?.b ?? ""}">${faceOf(coin.last?.b)}</div>
     </div>
     <div class="actions">
       <button class="button" id="measure">${esc(c.measureLabel)}</button>
       <button class="button secondary" id="reset">${esc(c.resetLabel)}</button>
     </div>
     <p class="tally">${coin.tally.flips} measurements &middot; ${coin.tally.agreements} agreed &middot; ${rate}% agreement</p>
     <p class="disclaimer">${esc(c.disclaimer)}</p>`,
    { href: "#/", label: "Lessons" },
  );
  app.querySelectorAll<HTMLButtonElement>(".mode").forEach((b) =>
    b.addEventListener("click", () => {
      coin.mode = b.dataset.mode as Mode;
      coin.last = null;
      coin.tally = { flips: 0, agreements: 0 };
      coinView();
    }),
  );
  document.querySelector("#measure")!.addEventListener("click", () => {
    coin.last = flipPair(coin.mode);
    coin.tally = record(coin.tally, coin.last);
    coinView();
  });
  document.querySelector("#reset")!.addEventListener("click", () => {
    coin.last = null;
    coin.tally = { flips: 0, agreements: 0 };
    coinView();
  });
}

// ---------- router ----------

function route(): void {
  const hash = location.hash || "#/";
  const lesson = hash.match(/^#\/lesson\/([\w-]+)$/);
  if (lesson) return lessonView(lesson[1]);
  if (hash === "#/quiz") return quizView();
  if (hash === "#/coin") return coinView();
  lessonsView();
}

window.addEventListener("hashchange", route);
route();
