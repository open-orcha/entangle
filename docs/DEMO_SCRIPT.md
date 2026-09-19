# Orcha demo runbook - using the Entangle app

Three short, repeatable scenarios that show what [Orcha](https://github.com/open-orcha/orcha) does today, using this repo as the project the agents work on. Every step names the real button, screen, or command; narration lines are suggestions, not a script you must read verbatim.

| # | Scenario | The point it makes | Live runtime | Edited length |
| --- | --- | --- | --- | --- |
| A | One task graph, three platforms | Dependencies, parallel agents, a reviewer in the chain, human verification from the phone | 25-40 min | 4-5 min |
| B | Bug fix with a reviewer in the loop | The loop is the product: fix -> review -> "add a test" -> fix -> human verifies | 10-20 min | ~3 min |
| C | Ask before you build | Chat lane: quick answers inline, real work becomes a tracked task | 3-5 min | ~2 min |

Agents take real minutes to build real code, so each scenario lists what to record live and what to cut. Record everything, then edit down to the "Edited length" using the cut list at the end of each scenario.

---

## 0. One-time setup (about 20 minutes, do this before the first recording)

### 0.1 Prerequisites on the presenter's Mac

- Docker Desktop running; Homebrew; Claude Code or Codex installed and signed in.
- Xcode 16+, XcodeGen (`brew install xcodegen`), Android Studio (or a JDK 17+ and the Android SDK), Node 22. The README's three one-command builds must pass on this machine before you record: `npm run build`, `./gradlew assembleDebug`, `xcodebuild ... build`.
- `gh auth status` shows a GitHub account that can push to the demo repo. Use a fork of `open-orcha/entangle` if you do not want demo branches and PRs on the canonical repo.
- The Orcha mobile app installed on a phone that is on the same network as the Mac (needed for the "verify from the phone" beat).

### 0.2 Clone and reset to the demo starting point

```sh
git clone https://github.com/open-orcha/entangle.git
cd entangle
git checkout main
git reset --hard demo-start          # the tag that marks the clean starting point
```

### 0.3 Start Orcha inside the project

```sh
brew install open-orcha/orcha/orcha
export ORCHA_LLM_API_KEY="sk-ant-..."     # or ANTHROPIC_API_KEY
orcha init --objective "Ship Entangle on iOS, Android, and web" --as Kedar
```

`orcha init` prints an `api:` line such as `http://localhost:8000/`. That is the portal; keep it open in the browser. It also prints `export ORCHA_ALIAS=Kedar` - run that in the terminal tab you will use for slash commands.

### 0.4 Register the cast

Five agents. Register them either from the portal (**Agents** -> **+ New** -> the **Set up your workspace** form: **Agent name**, **Role**, **System prompt**, **Model**, **First task: Not yet** -> **Create agent**) or from a Claude Code tab in the repo with `/orcha-register-agent`.

| Alias | Role (short) | System prompt (paste as-is) |
| --- | --- | --- |
| `Writer` | Content author | You maintain `content/lessons.json`, the single content contract for the Entangle app. Write accurate, short physics content; no pop-science overclaims. When you add a field to the contract, keep it optional so existing apps keep building, and update `web/src/content.test.ts` so `npm test` passes. Work on a branch, open a PR, never merge. |
| `iOSDev` | iOS engineer (SwiftUI) | You own `ios/`. Build with `cd ios && xcodegen generate && xcodebuild -scheme Entangle -destination 'platform=iOS Simulator,name=iPhone 16' build` before opening a PR. Keep changes minimal and idiomatic SwiftUI. Work on a branch, open a PR, never merge. |
| `AndroidDev` | Android engineer (Compose) | You own `android/`. Build with `cd android && ./gradlew assembleDebug testDebugUnitTest` before opening a PR. Keep changes minimal and idiomatic Jetpack Compose. Work on a branch, open a PR, never merge. |
| `WebDev` | Web engineer (Vite + TS) | You own `web/`. Run `npm test && npm run build` in `web/` before opening a PR. Keep changes minimal; no new dependencies. Work on a branch, open a PR, never merge. When someone asks you a question in chat, answer it inline if it takes under a few minutes; if it is real work, file a task and point them at it. |
| `Reviewer` | Code reviewer | You review PRs opened by the other agents. Check the diff against the task's definition of done, run the platform's build and tests, and insist on a regression test for any bug fix. Reply on the task thread with either "clean" or a numbered list of blocking findings, then hand the task back to its author. You never merge and never mark a task done. |

Skill form, one per agent (run each in its own Claude Code tab, or pass `--alias`):

```
/orcha-register-agent Writer --role "Content author" --prompt "<prompt from the table>"
```

### 0.5 Set the gates

- Top bar -> **Autonomy** -> choose **Build to PR** ("Agents execute approved plans up to an open PR; you still merge."). Do not use **Full** for the demo: under Full a finished task auto-completes and you lose the verification beat.
- Top bar -> **Notifier** must read **Running**; if it reads **Paused**, agents will not wake.
- Top right: pick **Kedar** as the acting human. The **New** task button and the **Accept** / **Reject...** buttons stay disabled until an acting human is selected.

### 0.6 Pair the phone

Portal top bar -> **Pair phone** (also under **Settings** -> **Phone pairing**) shows a QR code. On the phone: open Orcha -> **Add your Orcha** -> scan the code (or **Can't scan? Enter the address**). The workspace appears under **My Orchas** with tabs **Home · Tasks · Requests · Agents · Search**.

Optional: Settings on the phone -> **Needs-you alerts** on. These are local alerts from a background check ("expect minutes to an hour, not instant"), so do not promise instant push on camera.

### 0.7 Screen layout for recording

Left: the portal (**Tasks** page). Right: a terminal in the repo running `git log --oneline --all --graph` or the web app (`cd web && npm run dev`, http://localhost:5173). Phone on a stand or mirrored to the Mac for Scenario A's last beat.

---

## Scenario A - "One task graph, three platforms" (edited length 4-5 min)

**Story.** One human describes a content change once. A content agent makes it. The moment the human verifies that work, three platform agents wake in parallel, each on its own worktree, each ending in a PR that a reviewer agent reads. The human verifies the last step from the phone.

**The change.** Add lesson 6, "Quantum teleportation", to `content/lessons.json`, with a new optional `keyTakeaways` list (3 bullets). Then each platform renders **Key takeaways** as a bulleted section under the lesson body. The new field is what makes the platform tasks real work rather than a free ride: the apps already render any lesson in the JSON, but none of them know how to draw the new bullets.

### Prerequisites

Setup section 0 done, repo at `demo-start`, portal on **Tasks**, phone paired, autonomy **Build to PR**.

### Steps

| Time | You do | Narration | What the audience sees |
| --- | --- | --- | --- |
| 0:00 | Show the running web app and the empty **Tasks** board. | "Entangle is a small app that exists three times - iOS, Android, web - all reading one content file. Today I want a sixth lesson, and I want it on every platform. I'm going to describe it once." | Board empty, app with 5 lessons. |
| 0:30 | **Tasks** -> **New**. Fill: **Title** `Add lesson 6: quantum teleportation (with keyTakeaways)`; **Description** `Add a sixth lesson to content/lessons.json with id "teleportation", a 3-4 paragraph body in the same voice as the others, one new quiz question keyed to it, and a new optional field keyTakeaways: string[] (3 bullets) on that lesson only. Update the counts in web/src/content.test.ts.`; **Definition of done** `content/lessons.json has 6 lessons and 6 quiz questions; lesson "teleportation" has keyTakeaways with 3 entries; existing lessons unchanged; cd web && npm test passes; PR open against main.`; **Assignee** `Writer`; expand **Protocol** -> **Hand-off to** `Kedar`. Click **Create task**. | "Title, what done means, who owns it. That definition of done is the contract - the agent doesn't get to decide it's finished; I do." | Toast `Task created · assigned to Writer`. Card appears under *In progress* within a minute as the notifier wakes Writer. |
| 1:30 | Create the three platform tasks the same way. Each: **Assignee** the platform agent, **Depends on** the content task (⌘-click in the multi-select), **Protocol** -> **Review chain** `<agent> -> Reviewer -> loop until clean -> Kedar`, **Hand-off to** `Reviewer`. Titles and DoDs below. | "These three can't start until the content exists, so I tell Orcha that. And I put a reviewer in the chain: nothing comes to me until a second agent has read it." | Three cards under *Waiting* (status `pending`). Toast `Task created — starts when dependencies clear`. |
| 3:00 | Wait. Show Writer's run feed (**Agents** -> Writer). | "Writer is in its own git worktree under `.orcha-worktrees/`. It can't step on anyone else." | Streaming run log; a branch `orcha/task-...` appears in the terminal's git graph. |
| ~8:00 | Writer's PR opens; the task flips to **Awaiting verification**. Open the card: **Result claimed by Writer**, **Definition of done**. Skim the PR diff. Click **Accept** -> **Accept**. | "Here's the gate. The agent claims it's done and shows its work; I decide. The moment I accept, watch the three waiting tasks." | Toast `Accepted · completed`. The three platform cards move from *Waiting* to *Ready*, then to *In progress* as iOSDev, AndroidDev, and WebDev wake. |
| ~9:00 | Show three run feeds side by side (or click between them). Terminal: `ls .orcha-worktrees/`. | "Three agents, three platforms, three worktrees, at the same time. Same content, same definition of done shape, different toolchains." | Three worktrees, three branches. |
| ~20:00 | First platform PR opens. The task's hand-off goes to **Reviewer**, who wakes, runs the build, and posts on the task thread. If Reviewer finds something, the author fixes it and hands back. | "The reviewer isn't decoration. It runs the same build the CI does and it can send the work back. Only when it says clean does the task come to me." | Task thread shows Reviewer's message; second commit on the PR if there were findings. |
| ~25:00 | Pick up the phone. **Home** -> **Needs you** -> **Review & decide**, or **Tasks** -> the task -> **AWAITING YOUR VERIFICATION** -> **Review & verify**. Read **Definition of done** and **Claimed result**. Tap **Approve & complete**. | "And I don't have to be at the desk for this part. Same queue, same gates, on the phone." | The card on the portal flips to *Completed* as you tap. |
| ~26:00 | Merge the PRs on GitHub (or from the portal's **GitHub** page). Pull main, rebuild one app, open lesson 6. | "Four agents, one description, three platforms. The thing I actually did was write down what done means and say yes twice." | Lesson 6 with Key takeaways bullets on screen. |

### Platform task text (copy-paste)

- **iOS** - Title `iOS: render keyTakeaways on the lesson screen`. DoD `LessonDetailView shows a "Key takeaways" heading and a bulleted list when a lesson has keyTakeaways, and nothing extra when it does not; xcodebuild for the iPhone simulator passes; PR open against main.`
- **Android** - Title `Android: render keyTakeaways on the lesson screen`. DoD `LessonDetailScreen shows a "Key takeaways" heading and a bulleted list when a lesson has keyTakeaways (default empty list in the model), and nothing extra when it does not; ./gradlew assembleDebug testDebugUnitTest passes; PR open against main.`
- **Web** - Title `Web: render keyTakeaways on the lesson page`. DoD `The lesson view shows a "Key takeaways" heading and a <ul> when a lesson has keyTakeaways, and nothing extra when it does not; npm test and npm run build pass; PR open against main.`

### Expected agent behavior

- Writer edits only `content/lessons.json` and `web/src/content.test.ts`, runs `npm test`, pushes `orcha/task-<slug>`, opens a PR whose body starts `> 🧑 Triggered by @<your GitHub handle> via Orcha task <id>`, then calls `/orcha-done`. The task parks at `needs_verification` (autonomy is Build to PR).
- Platform agents stay `pending` until your **Accept**; the status log shows `status_changed -> ready, reason: deps satisfied`. Each builds locally before opening its PR. Expect 8-15 minutes each, in parallel.
- Reviewer posts on each task thread. A "clean" verdict is normal; a finding (usually "the empty-list case is not handled") sends the task back once.
- Nothing merges on its own. Nothing completes without a human tap.

### Cut list for the 4-5 minute edit

Keep 0:00-1:30 in full (the promise). Time-lapse the Writer wait to ~10 s. Keep the **Accept** click and the three cards flipping in real time (the money shot, ~20 s). Time-lapse the parallel builds with the three run feeds on screen (~20 s). Keep the Reviewer thread message (~15 s), the phone approval (~30 s), and lesson 6 on a device (~15 s).

### Reset

```sh
# close the demo PRs and delete their branches on GitHub
gh pr list --state open --json number --jq '.[].number' | xargs -n1 gh pr close --delete-branch
# local repo back to the starting point
git checkout main && git reset --hard demo-start
git worktree prune && rm -rf .orcha-worktrees
git branch | grep 'orcha/task-' | xargs -r git branch -D
# if you merged the PRs during the take, put the remote back too
git push --force origin demo-start:main
```

In the portal, completed demo tasks can stay on the board between takes. For an empty board, wipe the demo project's Orcha data and start over (this is the demo repo's own Orcha stack, nothing else): `orcha init --force --reset-data --objective "Ship Entangle on iOS, Android, and web" --as Kedar`, then re-register the cast (0.4), set the gates (0.5), and re-pair the phone (0.6). See "Full reset" at the end.

---

## Scenario B - "Bug fix with a reviewer in the loop" (edited length ~3 min)

**Story.** A user reports that the web quiz says "out of 4" when there are five questions. WebDev fixes it and opens a PR. Reviewer sends it back: no regression test. WebDev adds the test, hands back, Reviewer says clean, and the human verifies. The point: the loop is the product, and the review step has teeth.

**The bug.** The branch `demo/quiz-score-bug` carries one commit on top of `demo-start` that makes `web/src/main.ts` print `questions.length - 1` on the result screen.

### Prerequisites

Setup done, cast registered, autonomy **Build to PR**. Plant the bug:

```sh
git checkout main && git reset --hard demo-start
git merge --ff-only origin/demo/quiz-score-bug      # main now has the bug
git push origin main                                 # agents work from the pushed main
cd web && npm run dev                                # keep this running for the reveal
```

Open http://localhost:5173/#/quiz, answer five questions, and confirm the result reads "out of 4".

### Steps

| Time | You do | Narration | What the audience sees |
| --- | --- | --- | --- |
| 0:00 | Show the result screen with "out of 4". | "Five questions, and the app says I scored out of four. Let's file it the way a real bug gets filed." | The bug. |
| 0:20 | **Tasks** -> **New**. **Title** `Quiz result says "out of 4" for a 5-question quiz`; **Description** `On web, finish the quiz: the result screen shows the total as one less than the number of questions. Fix it and make sure it cannot regress.`; **Definition of done** `Result screen shows "out of 5" with the current content; a unit test covers the total; npm test and npm run build pass; PR open against main.`; **Assignee** `WebDev`; **Protocol** -> **Review chain** `WebDev -> Reviewer -> loop until clean -> Kedar`, **Hand-off to** `Reviewer`. **Create task**. | "Notice I didn't say where the bug is. And I put the reviewer in the chain again." | Card under *In progress*. |
| ~4:00 | WebDev's PR opens; the task hands off to Reviewer. Open the task thread. | "First fix is up. It's probably right. The question is whether it's protected." | PR with a one-line diff. |
| ~7:00 | Reviewer posts a finding: the fix has no test. The task goes back to WebDev. | "The reviewer doesn't accept 'trust me'. It asks for the test the definition of done asked for." | Thread: numbered finding. WebDev wakes again. |
| ~11:00 | Second commit lands: a small `scoreLine`-style helper plus a Vitest case. Reviewer replies clean. Task flips to **Awaiting verification**. | "Second pass. Test in place. Now, and only now, it comes to me." | PR shows 2 commits; CI green. |
| ~12:00 | On the portal or the phone: **Accept** / **Approve & complete**. Merge the PR. Refresh the quiz. | "Out of five. And the next person who breaks this gets told by a test, not by a user." | Result screen reads "out of 5". |

### Expected agent behavior

- WebDev finds the `- 1` in `web/src/main.ts` within its first minutes, fixes it, runs `npm test && npm run build`, opens the PR, hands off to Reviewer via a task request (not `/orcha-done` - the chain says Reviewer first).
- Reviewer runs the tests, notices there is no test for the total, and sends the task back with one numbered finding.
- WebDev extracts the result text into a small pure function and adds a Vitest case that asserts `out of 5` for the current content, then hands back. Reviewer says clean and WebDev calls `/orcha-done` -> `needs_verification`.
- If WebDev adds the test on the first pass (it sometimes will, because the DoD asks for one), Reviewer says clean immediately and you lose the "sent back" beat. To force the beat, drop "a unit test covers the total" from the DoD and rely on Reviewer's standing prompt ("insist on a regression test for any bug fix").

### Cut list for the ~3 minute edit

Bug reveal (15 s), task creation (30 s), time-lapse to first PR (10 s), Reviewer's finding on the thread (20 s), time-lapse to second commit (10 s), clean verdict + Accept (20 s), the "out of 5" reveal (10 s).

### Reset

```sh
gh pr list --state open --json number --jq '.[].number' | xargs -n1 gh pr close --delete-branch
git checkout main && git reset --hard demo-start
git push --force origin demo-start:main          # removes the planted bug from the remote
git worktree prune && rm -rf .orcha-worktrees
git branch | grep 'orcha/task-' | xargs -r git branch -D
```

---

## Scenario C - "Ask before you build" (edited length ~2 min)

**Story.** Not everything is a task. The human asks an agent a question in chat; the agent answers inline in a minute because it is a small question. Then the human says "do it", and the agent turns that into a tracked task and points at the thread. Two lanes, one conversation.

### Prerequisites

Setup done; `WebDev` registered with the prompt from section 0.4 (its last sentence is what makes this scenario work).

### Steps

| Time | You do | Narration | What the audience sees |
| --- | --- | --- | --- |
| 0:00 | **Agents** -> **WebDev**. In the composer (`Message WebDev — type / for skills…`) type: `What would it take to add a Spanish translation of the lessons? Rough scope only, don't build anything.` -> **Send**. | "Sometimes I just want to think out loud with the person who owns the code." | Turn appears; WebDev's reply arrives in under a minute. |
| 0:45 | Read the reply aloud, briefly. | "That's a scoping answer, inline, in the chat. No task, no PR, no ceremony - because it didn't need any." | A short, concrete answer: a `lang` field or a second JSON file, a language toggle in each app, ~an hour per platform, and an offer to file it. |
| 1:15 | Type: `Yes - file it for web only, Spanish, keep the toggle simple.` -> **Send**. | "Now it's real work. Watch the routing change." | WebDev replies with one line: `I'll handle this in the background — follow the task thread: <task-id>`. A new card appears on **Tasks**, assigned to WebDev. |
| 1:45 | Click through to the task. Show the definition of done the agent wrote. | "Same agent, two lanes. Chat for thinking, tasks for anything that touches code - with a definition of done I can hold it to." | Task card with DoD, already *In progress*. |
| 2:00 | Optional on the phone: **Agents** -> WebDev -> **Converse** to show the same thread on mobile. | "And the conversation follows me." | Same turns on the phone. |

### Expected agent behavior

- First message: answered inline (the agent's conversation lane rule: a question or estimate that fits in a message or two is QUICK - reply, do not file).
- Second message: the agent creates a task assigned to itself with a title and DoD, replies with the one-line pointer, and stops. Its worker then wakes on the task as usual.

### Reset

Open the new task on **Tasks** and leave it, or cancel it from a Claude Code tab: `/orcha-close <task_id>`. On **Agents** -> WebDev, the header's **End conversation** (phone: overflow menu -> **End conversation**) starts the next take with an empty thread.

---

## Troubleshooting on camera

| Symptom | Fix |
| --- | --- |
| **New** / **Accept** disabled | Pick the acting human in the top-right of the portal. |
| A task sits at *Ready* and nobody wakes | Top bar **Notifier** says **Paused** - click it to **Running**. Or **Requests** -> the pending request -> **Nudge**. |
| A finished task jumped straight to *Completed* | Autonomy was **Full**. Switch to **Build to PR** and re-run the scenario. |
| Platform tasks stayed *Waiting* after Accept | They depend on a task that is not the one you accepted. Open the card and check **Depends on**. |
| The agent opened the PR against the wrong repo | `gh auth status` on the Mac must show an account with push access to the repo the clone points at. |
| iOS build fails on a fresh Mac | `sudo xcodebuild -license accept` once, then `brew install xcodegen`. |

## Full reset (between recording sessions)

```sh
git checkout main && git reset --hard demo-start
git push --force origin demo-start:main
git worktree prune && rm -rf .orcha-worktrees
git branch | grep 'orcha/task-' | xargs -r git branch -D
gh pr list --state open --json number --jq '.[].number' | xargs -n1 gh pr close --delete-branch
# Empty board: drops ONLY this demo project's Orcha database and creates a fresh container.
orcha init --force --reset-data --objective "Ship Entangle on iOS, Android, and web" --as Kedar
```

Then re-register the cast (0.4), set the gates (0.5), and re-pair the phone (0.6). If you only want to stop the stack between sessions without wiping anything, `orcha down` stops it and `orcha up` brings the same board back.
