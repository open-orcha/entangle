# Orcha demo runbook - using the Entangle app

Three short, repeatable scenarios that show what [Orcha](https://github.com/open-orcha/orcha) does today, using this repo as the project the agents work on. Every step names the real button, screen, or command; narration lines are suggestions, not a script you must read verbatim.

**The cast is four agents and one human, everywhere in this document:** `Atlas` (orchestrator - owns the content contract and reviews the platform PRs), `iOS Dev`, `Android Dev`, `Web Dev`, and you, `Kedar`. No other agents.

**Every scenario touches all three Orcha surfaces:** the web portal, the Orcha mobile app, and the CLI (the `orcha` command plus the `/orcha-*` slash commands inside a Claude Code tab). The "Surface" column in each step table says which one is on screen.

| # | Scenario | The point it makes | Live runtime | Edited length |
| --- | --- | --- | --- | --- |
| A | One task graph, three platforms | Dependencies, parallel agents, a reviewer in the chain, human verification from the phone | 25-40 min | 4-5 min |
| B | Bug fix with a reviewer in the loop | The loop is the product: fix -> review -> "add a test" -> fix -> human verifies | 10-20 min | ~3 min |
| C | Ask before you build | Chat lane: quick answers inline, real work becomes a tracked task for the right agent | 3-5 min | ~2 min |

Agents take real minutes to build real code, so each scenario lists what to record live and what to cut. Record everything, then edit down to the "Edited length" using the cut list at the end of each scenario.

---

## 0. One-time setup (about 20 minutes, do this before the first recording)

### 0.1 Prerequisites on the presenter's Mac

- Docker Desktop running; Homebrew; Claude Code (or Codex) installed and signed in.
- Xcode 16+, XcodeGen (`brew install xcodegen`), Android Studio (or a JDK 17+ and the Android SDK), Node 22. The README's three one-command builds must pass on this machine before you record: `npm run build`, `./gradlew assembleDebug`, `xcodebuild ... build`.
- `gh auth status` shows a GitHub account that can push to the demo repo. Use a fork of `open-orcha/entangle` if you do not want demo branches and PRs on the canonical repo.
- The Orcha mobile app installed on a phone that is on the same network as the Mac.

### 0.2 Clone the repo and check out the demo starting point

```sh
git clone https://github.com/open-orcha/entangle.git
cd entangle
git checkout main
git reset --hard demo-start          # the tag that marks the clean starting point
```

### 0.3 Create the Orcha project (CLI)

```sh
brew install open-orcha/orcha/orcha
export ORCHA_LLM_API_KEY="sk-ant-..."     # or ANTHROPIC_API_KEY
orcha init --objective "Ship Entangle on iOS, Android, and web" --as Kedar --github <your GitHub handle>
```

`orcha init` prints an `api:` line such as `http://localhost:8000/` - that is the portal; keep it open in a browser. It also prints `export ORCHA_ALIAS=Kedar`: run that in the terminal tab you will use for slash commands, then open Claude Code in the repo (`claude`). That tab is "the CLI" for the rest of this document. `orcha status` prints the stack and the portal URL any time you need them on camera.

### 0.4 Register the cast (CLI)

Run these four commands in the Claude Code tab, one at a time. (Portal alternative: **Agents** -> **+ New** -> the **Set up your workspace** form: **Agent name**, **Role**, **System prompt**, **Model**, **First task: Not yet** -> **Create agent**.)

```
/orcha-register-agent Atlas --role "Orchestrator, content owner, reviewer" --prompt "You are Atlas, the orchestrator for the Entangle app (iOS, Android, web sharing content/lessons.json). You own content/lessons.json: write accurate, short physics content, no pop-science overclaims; when you add a field to the contract keep it optional so existing apps keep building, and update web/src/content.test.ts so npm test passes. You review pull requests opened by iOS Dev, Android Dev, and Web Dev: check the diff against the task's definition of done, run that platform's build and tests, insist on a regression test for any bug fix, reply on the task thread with either 'clean' or a numbered list of blocking findings, then hand the task back to its author. In chat, answer scoping questions inline in a few sentences; when the human says to go ahead, file the task with /orcha-task-new assigned to the platform agent who owns that directory (Web Dev for web/, iOS Dev for ios/, Android Dev for android/) with a clear definition of done, and reply with the task id. Work on a branch, open a PR, never merge, never mark another agent's task done."
```

```
/orcha-register-agent "iOS Dev" --role "iOS engineer (SwiftUI)" --prompt "You own ios/ in the Entangle app. Build with: cd ios && xcodegen generate && xcodebuild -scheme Entangle -destination 'platform=iOS Simulator,name=iPhone 16' build - before opening a PR. Keep changes minimal and idiomatic SwiftUI. Work on a branch, open a PR, never merge. Follow the task's review chain: hand off to Atlas for review before calling done, and fix every numbered finding Atlas posts."
```

```
/orcha-register-agent "Android Dev" --role "Android engineer (Compose)" --prompt "You own android/ in the Entangle app. Build with: cd android && ./gradlew assembleDebug testDebugUnitTest - before opening a PR. Keep changes minimal and idiomatic Jetpack Compose. Work on a branch, open a PR, never merge. Follow the task's review chain: hand off to Atlas for review before calling done, and fix every numbered finding Atlas posts."
```

```
/orcha-register-agent "Web Dev" --role "Web engineer (Vite + TypeScript)" --prompt "You own web/ in the Entangle app. Run npm test && npm run build in web/ before opening a PR. Keep changes minimal; no new dependencies. Work on a branch, open a PR, never merge. Follow the task's review chain: hand off to Atlas for review before calling done, and fix every numbered finding Atlas posts."
```

Aliases with a space (`iOS Dev`, `Android Dev`, `Web Dev`) must be quoted in every command, exactly as above. Check the result on the portal's **Agents** page: four cards, `Atlas`, `iOS Dev`, `Android Dev`, `Web Dev`.

### 0.5 Set the gates (portal)

- Top bar -> **Autonomy** -> choose **Build to PR** ("Agents execute approved plans up to an open PR; you still merge."). Do not use **Full** for the demo: under Full a finished task auto-completes and you lose the verification beat.
- Top bar -> **Notifier** must read **Running**; if it reads **Paused**, agents will not wake.
- Top right: pick **Kedar** as the acting human. The **New** task button and the **Accept** / **Reject...** buttons stay disabled until an acting human is selected.

### 0.6 Pair the phone (mobile)

Portal top bar -> **Pair phone** (also under **Settings** -> **Phone pairing**) shows a QR code. On the phone: open Orcha -> **Add your Orcha** -> scan the code (or **Can't scan? Enter the address**). The workspace appears under **My Orchas** with tabs **Home · Tasks · Requests · Agents · Search**.

Optional: Settings on the phone -> **Needs-you alerts** on. These are local alerts from a background check ("expect minutes to an hour, not instant"), so do not promise instant push on camera.

### 0.7 Screen layout for recording

Left: the portal (**Tasks** page). Right: the Claude Code tab (for `/orcha-*` commands) with a second terminal tab running `git log --oneline --all --graph` or the web app (`cd web && npm run dev`, http://localhost:5173). Phone on a stand or mirrored to the Mac.

### 0.8 What must never be on screen

Only this repo, this Orcha project, and the four agents above. Every task title, branch name, PR, and chat message in the recordings is about the Entangle app. If the Mac has other Orcha projects, record inside a fresh clone with its own `orcha init` (0.3) so the board starts empty.

---

## Scenario A - "One task graph, three platforms" (edited length 4-5 min)

**Story.** One human describes a content change once and builds the task graph from the CLI. Atlas makes the content change. The moment the human verifies it, three platform agents wake in parallel, each on its own worktree, each ending in a PR that Atlas reviews. The human verifies the last step from the phone.

**The change.** Add lesson 6, "Quantum teleportation", to `content/lessons.json`, with a new optional `keyTakeaways` list (3 bullets). Then each platform renders **Key takeaways** as a bulleted section under the lesson body. The new field is what makes the platform tasks real work rather than a free ride: the apps already render any lesson in the JSON, but none of them know how to draw the new bullets.

### Prerequisites

Setup section 0 done, repo at `demo-start`, portal on **Tasks**, phone paired, autonomy **Build to PR**, Claude Code tab open with `ORCHA_ALIAS=Kedar`.

### Steps

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | web app + portal | Show the running web app and the empty **Tasks** board. | "Entangle is a small app that exists three times - iOS, Android, web - all reading one content file. Today I want a sixth lesson, and I want it on every platform. I'm going to describe it once." | Board empty, app with 5 lessons. |
| 0:30 | CLI | In the Claude Code tab, create the content task (command 1 below). Copy the `task_id` it prints. | "Title, what done means, who owns it. That definition of done is the contract - the agent doesn't get to decide it's finished; I do." | The command reports `task_id`, status `in_progress`, assignee `Atlas`, and a portal link. |
| 1:15 | CLI | Create the three platform tasks (commands 2-4 below), pasting the content task's id after `--depends-on`. | "These three can't start until the content exists, so I tell Orcha that. And I put a reviewer in the chain: nothing comes to me until Atlas has read it." | Each reports status `pending` and `depends_on: 1`. |
| 2:00 | portal | Switch to the **Tasks** board. | "Same graph, on the board." | One card under *In progress* (Atlas), three under *Waiting* (`pending`). |
| 3:00 | portal | Wait. Show Atlas's run feed (**Agents** -> **Atlas**). | "Atlas is in its own git worktree under `.orcha-worktrees/`. It can't step on anyone else." | Streaming run log; a branch `orcha/task-...` appears in the terminal's git graph. |
| ~8:00 | portal | Atlas's PR opens; the task flips to **Awaiting verification**. Open the card: **Result claimed by Atlas**, **Definition of done**. Skim the PR diff. Click **Accept** -> **Accept**. | "Here's the gate. The agent claims it's done and shows its work; I decide. The moment I accept, watch the three waiting tasks." | Toast `Accepted · completed`. The three platform cards move from *Waiting* to *Ready*, then to *In progress* as `iOS Dev`, `Android Dev`, and `Web Dev` wake. |
| ~9:00 | portal + terminal | Show three run feeds side by side (or click between them). Terminal: `ls .orcha-worktrees/`. | "Three agents, three platforms, three worktrees, at the same time. Same content, same definition-of-done shape, different toolchains." | Three worktrees, three branches. |
| ~20:00 | CLI | First platform PR opens and the task hands off to Atlas. In the Claude Code tab: `/orcha-thread <that task_id>`. | "The reviewer isn't decoration. Atlas runs the same build the CI does and it can send the work back. Only when it says clean does the task come to me." | The thread printed in the terminal: the dev's hand-off, then Atlas's `clean` or numbered findings; a second commit on the PR if there were findings. |
| ~25:00 | mobile | Pick up the phone. **Home** -> **Needs you** -> **Review & decide**, or **Tasks** -> the task -> **AWAITING YOUR VERIFICATION** -> **Review & verify**. Read **Definition of done** and **Claimed result**. Tap **Approve & complete**. | "And I don't have to be at the desk for this part. Same queue, same gates, on the phone." | The card on the portal flips to *Completed* as you tap. |
| ~26:00 | GitHub + app | Merge the PRs on GitHub (or from the portal's **GitHub** page). Pull main, rebuild one app, open lesson 6. | "Four agents, one description, three platforms. The thing I actually did was write down what done means and say yes twice." | Lesson 6 with Key takeaways bullets on screen. |

### The four commands (copy-paste into the Claude Code tab, one at a time)

Command 1 - the content task, assigned to Atlas, handed to you:

```
/orcha-task-new "Add lesson 6: quantum teleportation (with keyTakeaways)" --assign Atlas --handoff-to Kedar --description "Add a sixth lesson to content/lessons.json with id \"teleportation\", a 3-4 paragraph body in the same voice as the others, one new quiz question keyed to it, and a new optional field keyTakeaways: string[] (3 bullets) on that lesson only. Update the counts in web/src/content.test.ts." --dod "content/lessons.json has 6 lessons and 6 quiz questions; lesson \"teleportation\" has keyTakeaways with 3 entries; existing lessons unchanged; cd web && npm test passes; PR open against main."
```

Commands 2-4 - the platform tasks, each depending on the content task and reviewed by Atlas (replace `<content-task-id>` with the id command 1 printed):

```
/orcha-task-new "iOS: render keyTakeaways on the lesson screen" --assign "iOS Dev" --depends-on <content-task-id> --review-chain "iOS Dev -> Atlas -> loop until clean -> Kedar" --handoff-to Atlas --dod "LessonDetailView shows a \"Key takeaways\" heading and a bulleted list when a lesson has keyTakeaways, and nothing extra when it does not; xcodebuild for the iPhone simulator passes; PR open against main."
```

```
/orcha-task-new "Android: render keyTakeaways on the lesson screen" --assign "Android Dev" --depends-on <content-task-id> --review-chain "Android Dev -> Atlas -> loop until clean -> Kedar" --handoff-to Atlas --dod "LessonDetailScreen shows a \"Key takeaways\" heading and a bulleted list when a lesson has keyTakeaways (default empty list in the model), and nothing extra when it does not; ./gradlew assembleDebug testDebugUnitTest passes; PR open against main."
```

```
/orcha-task-new "Web: render keyTakeaways on the lesson page" --assign "Web Dev" --depends-on <content-task-id> --review-chain "Web Dev -> Atlas -> loop until clean -> Kedar" --handoff-to Atlas --dod "The lesson view shows a \"Key takeaways\" heading and a <ul> when a lesson has keyTakeaways, and nothing extra when it does not; npm test and npm run build pass; PR open against main."
```

Portal alternative for any of them: **Tasks** -> **New**, fill **Title**, **Description**, **Definition of done**, **Assignee**, **Depends on** (multi-select), and expand **Protocol** for **Review chain** and **Hand-off to** -> **Create task**.

### Expected agent behavior

- Atlas edits only `content/lessons.json` and `web/src/content.test.ts`, runs `npm test`, pushes `orcha/task-<slug>`, opens a PR whose body starts `> 🧑 Triggered by @<your GitHub handle> via Orcha task <id>`, then calls `/orcha-done`. The task parks at `needs_verification` (autonomy is Build to PR).
- Platform agents stay `pending` until your **Accept**; the status log shows `status_changed -> ready, reason: deps satisfied`. Each builds locally before opening its PR. Expect 8-15 minutes each, in parallel.
- Each platform task hands off to Atlas, who posts on the task thread. A `clean` verdict is normal; a finding (usually "the empty-list case is not handled") sends the task back once.
- Nothing merges on its own. Nothing completes without a human tap.

### Optional variant - let Atlas build the graph

If you want the orchestrator on camera doing the orchestrating: skip commands 1-4, open **Agents** -> **Atlas** and send `Add lesson 6, quantum teleportation, with a keyTakeaways list, to all three apps. Do the content yourself, then file one task per platform depending on yours, with you as reviewer.` Atlas files the same four tasks with `/orcha-task-new`. It is a stronger story and a less predictable take: check the board before continuing, and if the graph is wrong, do the Full reset and fall back to commands 1-4 (tasks cannot be deleted once created).

### Cut list for the 4-5 minute edit

Keep 0:00-2:00 in full (the promise, typed as four commands). Time-lapse the Atlas wait to ~10 s. Keep the **Accept** click and the three cards flipping in real time (the money shot, ~20 s). Time-lapse the parallel builds with the three run feeds on screen (~20 s). Keep the `/orcha-thread` review exchange (~15 s), the phone approval (~30 s), and lesson 6 on a device (~15 s).

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

In the portal, completed demo tasks can stay on the board between takes. For an empty board, wipe the demo project's Orcha data and start over (this is the demo repo's own Orcha stack, nothing else): `orcha init --force --reset-data --objective "Ship Entangle on iOS, Android, and web" --as Kedar --github <your GitHub handle>`, then re-register the cast (0.4), set the gates (0.5), and re-pair the phone (0.6). See "Full reset" at the end.

---

## Scenario B - "Bug fix with a reviewer in the loop" (edited length ~3 min)

**Story.** A user reports that the web quiz says "out of 4" when there are five questions. `Web Dev` fixes it and opens a PR. Atlas sends it back: no regression test. `Web Dev` adds the test and hands back, Atlas says clean, and the human verifies from the phone. The point: the loop is the product, and the review step has teeth.

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

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | web app | Show the result screen with "out of 4". | "Five questions, and the app says I scored out of four. Let's file it the way a real bug gets filed." | The bug. |
| 0:20 | portal | **Tasks** -> **New**. **Title** `Quiz result says "out of 4" for a 5-question quiz`; **Description** `On web, finish the quiz: the result screen shows the total as one less than the number of questions. Fix it and make sure it cannot regress.`; **Definition of done** `Result screen shows "out of 5" with the current content; a unit test covers the total; npm test and npm run build pass; PR open against main.`; **Assignee** `Web Dev`; expand **Protocol** -> **Review chain** `Web Dev -> Atlas -> loop until clean -> Kedar`, **Hand-off to** `Atlas`. **Create task**. | "Notice I didn't say where the bug is. And I put the reviewer in the chain again." | Toast `Task created · assigned to Web Dev`; card under *In progress*. |
| ~4:00 | portal | `Web Dev`'s PR opens; the task hands off to Atlas. | "First fix is up. It's probably right. The question is whether it's protected." | PR with a one-line diff. |
| ~7:00 | CLI | Atlas posts a finding. In the Claude Code tab: `/orcha-thread <task_id>` (the id is on the task card). | "The reviewer doesn't accept 'trust me'. It asks for the test the definition of done asked for." | The thread in the terminal: Atlas's numbered finding, the task handed back; `Web Dev` wakes again. |
| ~11:00 | portal | Second commit lands: a small `scoreLine`-style helper plus a Vitest case. Atlas replies clean. Task flips to **Awaiting verification**. | "Second pass. Test in place. Now, and only now, it comes to me." | PR shows 2 commits; CI green. |
| ~12:00 | mobile | On the phone: **Tasks** -> the task -> **Review & verify** -> **Approve & complete**. Then merge the PR on GitHub and refresh the quiz. | "Out of five. And the next person who breaks this gets told by a test, not by a user." | Card flips to *Completed*; result screen reads "out of 5". |

### Expected agent behavior

- `Web Dev` finds the `- 1` in `web/src/main.ts` within its first minutes, fixes it, runs `npm test && npm run build`, opens the PR, hands off to Atlas via a task request (not `/orcha-done` - the chain says Atlas first).
- Atlas runs the tests, notices there is no test for the total, and sends the task back with one numbered finding.
- `Web Dev` extracts the result text into a small pure function and adds a Vitest case that asserts `out of 5` for the current content, then hands back. Atlas says clean and `Web Dev` calls `/orcha-done` -> `needs_verification`.
- If `Web Dev` adds the test on the first pass (it sometimes will, because the DoD asks for one), Atlas says clean immediately and you lose the "sent back" beat. To force the beat, drop "a unit test covers the total" from the DoD and rely on Atlas's standing prompt ("insist on a regression test for any bug fix").

### Cut list for the ~3 minute edit

Bug reveal (15 s), task creation (30 s), time-lapse to first PR (10 s), Atlas's finding via `/orcha-thread` (20 s), time-lapse to second commit (10 s), clean verdict + phone approval (20 s), the "out of 5" reveal (10 s).

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

**Story.** Not everything is a task. The human asks the orchestrator a question in chat; Atlas answers inline in a minute because it is a small question. Then the human says "do it", and Atlas turns that into a tracked task for the agent who owns that code and points at the thread. Two lanes, one conversation - and the task shows up on every surface.

### Prerequisites

Setup done; `Atlas` and `Web Dev` registered with the prompts from section 0.4 (Atlas's chat sentence is what makes this scenario work).

### Steps

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | portal | **Agents** -> **Atlas**. In the composer (`Message Atlas — type / for skills…`) type: `What would it take to add a Spanish translation of the lessons? Rough scope only, don't build anything.` -> **Send**. | "Sometimes I just want to think out loud with the one who runs the team." | Turn appears; Atlas's reply arrives in under a minute. |
| 0:45 | portal | Read the reply aloud, briefly. | "That's a scoping answer, inline, in the chat. No task, no PR, no ceremony - because it didn't need any." | A short, concrete answer: a `lang` field or a second JSON file, a language toggle in each app, roughly an hour per platform, and an offer to file it. |
| 1:15 | portal | Type: `Yes - file it for web only, Spanish, keep the toggle simple.` -> **Send**. | "Now it's real work. Watch the routing change." | Atlas replies with one line and a task id. A new card appears on **Tasks**, assigned to `Web Dev`. |
| 1:45 | CLI + portal | In the Claude Code tab: `/orcha-status`. Then click the new card on **Tasks**. | "Same conversation, and the task is already on the board with a definition of done I can hold Web Dev to." | The status summary lists the new task, `in_progress`, next to the others; the card shows the definition of done Atlas wrote and `Web Dev` as assignee. |
| 2:15 | mobile | On the phone: **Agents** -> **Atlas** -> **Converse**. Scroll to the two turns. | "And the conversation follows me." | The same two questions and two answers on the phone. |

### Expected agent behavior

- First message: answered inline (the agent's conversation-lane rule: a question or estimate that fits in a message or two is QUICK - reply, do not file).
- Second message: Atlas creates a task with `/orcha-task-new ... --assign "Web Dev"` with a title and definition of done, replies with the one-line pointer, and stops. `Web Dev`'s worker then wakes on the task as usual. If Atlas assigns the task to itself instead of `Web Dev`, the beat still works - the point on camera is chat turning into a tracked task - but you can say "Atlas, reassign that to Web Dev" and it will.

### Reset

Tasks cannot be deleted, so leave the new task on the board (or **Reject...** it with a one-line reason); for an empty board use the Full reset below. On **Agents** -> **Atlas**, the header's **End conversation** (phone: overflow menu -> **End conversation**) starts the next take with an empty thread.

---

## Troubleshooting on camera

| Symptom | Fix |
| --- | --- |
| **New** / **Accept** disabled | Pick the acting human in the top-right of the portal. |
| `/orcha-task-new` says the assignee alias is not registered | The alias is case- and space-sensitive: `"iOS Dev"`, `"Android Dev"`, `"Web Dev"`, `Atlas`, quoted exactly as in 0.4. |
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
orcha init --force --reset-data --objective "Ship Entangle on iOS, Android, and web" --as Kedar --github <your GitHub handle>
```

Then re-register the cast (0.4), set the gates (0.5), and re-pair the phone (0.6). If you only want to stop the stack between sessions without wiping anything, `orcha down` stops it and `orcha up` brings the same board back.
