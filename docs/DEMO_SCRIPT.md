# Orcha demo runbook - using the Entangle app

Six short, repeatable scenarios that show what [Orcha](https://github.com/open-orcha/orcha) does today, using this repo as the project the agents work on. Every step names the real button, screen, or command; narration lines are suggestions, not a script you must read verbatim.

**The cast is four agents and one human, everywhere in this document:** `Atlas` (orchestrator - owns the content contract, reviews and verifies the other agents' work), `iOS Dev`, `Android Dev`, `Web Dev`, and you, `Kedar`. No other agents.

**Every scenario touches all three Orcha surfaces:** the web portal, the Orcha mobile app, and the CLI (the `orcha` command plus the `/orcha-*` slash commands inside a Claude Code tab). The "Surface" column in each step table says which one is on screen.

| # | Scenario | The point it makes | Live runtime | Edited length |
| --- | --- | --- | --- | --- |
| A | Add a feature: one task graph, three platforms | Dependencies, parallel agents, a reviewer in the chain, human verification from the phone | 25-40 min | 4-5 min |
| B | Fix a bug with a reviewer in the loop | The loop is the product: fix -> review -> "add a test" -> fix -> human verifies | 10-20 min | ~3 min |
| C | Ask before you build | Chat lane: quick answers inline, real work becomes a tracked task for the right agent | 3-5 min | ~2 min |
| D | Run it: simulator + emulator, verified by a second agent | Agents build, install, and launch the apps on your Mac; another agent re-runs it before you see it | 10-15 min | ~3 min |
| E | Explore the code with an agent | Questions about the codebase get file-level answers, in chat and as a request - no task, no PR | 3-5 min | ~2 min |
| F | From a GitHub issue to a reviewed PR | Start an issue from the portal or the phone, watch the PR and its checks, verify, merge on GitHub | 15-25 min | ~4 min |

Which scenario shows what (the checklist this runbook was written against):

| You want to show | Scenario |
| --- | --- |
| Adding a new feature | A |
| Fixing a bug | B (web), F (Android) |
| Getting work verified by another agent | A, B, F (Atlas reviews the PR), D (Atlas re-runs the app itself) |
| Running the app inside Orcha, on the iPhone simulator and Android emulator | D |
| Exploring the code | E (and the scoping question in C) |
| Checking GitHub pull requests | F step 5, A's last step |
| Starting work on a GitHub issue | F |

Agents take real minutes to build real code, so each scenario lists what to record live and what to cut. Record everything, then edit down to the "Edited length" using the cut list at the end of each scenario.

---

## 0. One-time setup (about 25 minutes, do this before the first recording)

### 0.1 Prerequisites on the presenter's Mac

- Docker Desktop running; Homebrew; Claude Code (or Codex) installed and signed in.
- Xcode 16+, XcodeGen (`brew install xcodegen`), Android Studio (or a JDK 17+ and the Android SDK), Node 22. The README's three one-command builds must pass on this machine before you record: `npm run build`, `./gradlew assembleDebug`, `xcodebuild ... build`.
- Put the Android command-line tools and a working JDK on `PATH` before starting Orcha. These defaults match a standard Android Studio install on macOS (and the recording Mac used to verify this repo):
  ```sh
  export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
  export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
  export JAVA_HOME="$(/usr/libexec/java_home)"
  ```
- **A booted iPhone simulator and a booted Android emulator** (needed for D and F; nice to have for A). Check the names you have:
  ```sh
  xcrun simctl list devices available | grep iPhone      # e.g. "iPhone 16 (…) (Shutdown)"
  $ANDROID_HOME/emulator/emulator -list-avds             # e.g. Pixel_7_Pro_API_34
  ```
  This runbook uses `iPhone 16` and `Pixel_7_Pro_API_34`. If your names differ, substitute them in the agent prompts (0.4) and in every command below. Boot both before recording: `open -a Simulator` (then **File -> Open Simulator -> iPhone 16**) and `$ANDROID_HOME/emulator/emulator -avd Pixel_7_Pro_API_34 &`. `adb devices` must list the emulator.
- `gh auth status` shows a GitHub account that can push to the demo repo, and `gh issue create` works against it. **Use a disposable fork for A, B, C, and F:** those scenarios create branches, issues, and PRs, and some takes merge code before resetting. Never force-reset `open-orcha/entangle` itself.
- A GitHub personal access token for the same repo (fine-grained: Contents read, Issues read/write, Pull requests read/write, Checks read). The portal's **GitHub** page and the phone's GitHub hub read issues and PRs through it (0.5).
- The Orcha mobile app installed on a phone that is on the same network as the Mac.

### 0.2 Clone the repo and check out the demo starting point

For the full set of write scenarios, make a disposable fork. With GitHub CLI this is:

```sh
gh repo fork open-orcha/entangle --clone
cd entangle
git fetch upstream --tags
git checkout main
git reset --hard demo-start          # the tag that marks the clean starting point
git push --force-with-lease origin main
gh repo set-default <your GitHub handle>/entangle
```

`origin` must be your fork and `upstream` must be `open-orcha/entangle`; confirm with `git remote -v`. `gh repo set-default` matters because the GitHub CLI otherwise prefers `upstream` after `gh repo fork`. Open the fork's **Actions** tab once and enable workflows before recording, so PR checks run. For read-only scenarios D and E, a normal clone of `open-orcha/entangle` is enough.

### 0.3 Create the Orcha project (CLI)

Run this **inside the clone**: `orcha init` binds the checkout's github.com `origin` remote to the new project automatically, which is what makes the **GitHub** page and the phone's GitHub hub show this repo.

```sh
brew install open-orcha/orcha/orcha
export ORCHA_LLM_API_KEY="sk-ant-..."     # or ANTHROPIC_API_KEY
orcha init --objective "Ship Entangle on iOS, Android, and web" --as Kedar --github <your GitHub handle>
```

`--github` is your GitHub username; agents put it in the first line of every PR they open (`> 🧑 Triggered by @<you> via Orcha task <id>`). `orcha init` prints an `api:` line such as `http://localhost:8000/` - that is the portal; keep it open in a browser. It also prints `export ORCHA_ALIAS=Kedar`: run that in the terminal tab you will use for slash commands, then open Claude Code in the repo (`claude`). That tab is "the CLI" for the rest of this document. `orcha status` prints the stack and the portal URL any time you need them on camera.

### 0.4 Register the cast (CLI)

Run these four commands in the Claude Code tab, one at a time. (Portal alternative: **Agents** -> **+ New** -> the **Set up your workspace** form: **Agent name**, **Role**, **System prompt**, **Model**, **First task: Not yet** -> **Create agent**.)

```
/orcha-register-agent Atlas --role "Orchestrator, content owner, reviewer" --prompt "You are Atlas, the orchestrator for the Entangle app (iOS, Android, web sharing content/lessons.json). You own content/lessons.json: write accurate, short physics content, no pop-science overclaims; when you add a field to the contract keep it optional so existing apps keep building, and update web/src/content.test.ts so npm test passes. You review pull requests opened by iOS Dev, Android Dev, and Web Dev: check the diff against the task's definition of done, run that platform's build and tests, insist on a regression test for any bug fix, reply on the task thread with either 'clean' or a numbered list of blocking findings, then hand the task back to its author. When a task asks you to verify that an app runs, do not trust the thread: build it yourself, install and launch it on the booted simulator or emulator (ios: xcodegen generate, xcodebuild with -derivedDataPath build, xcrun simctl install/launch booted io.openorcha.entangle; android: ./gradlew installDebug, adb shell am start -n io.openorcha.entangle/.MainActivity), take a screenshot, and say on the thread what the screen shows. In chat, answer questions about the code with file paths and function names, and answer scoping questions inline in a few sentences; when asked about GitHub, use gh pr list, gh pr checks, and gh issue list and summarize; when the human says to go ahead, file the task with /orcha-task-new assigned to the platform agent who owns that directory (Web Dev for web/, iOS Dev for ios/, Android Dev for android/) with a clear definition of done, and reply with the task id. Work on a branch, open a PR, never merge, never mark another agent's task done."
```

```
/orcha-register-agent "iOS Dev" --role "iOS engineer (SwiftUI)" --prompt "You own ios/ in the Entangle app. Build with: cd ios && xcodegen generate && xcodebuild -scheme Entangle -destination 'platform=iOS Simulator,name=iPhone 16' -derivedDataPath build build - before opening a PR. To run the app: xcrun simctl boot 'iPhone 16' (ignore 'already booted'), open -a Simulator, xcrun simctl install booted build/Build/Products/Debug-iphonesimulator/Entangle.app, xcrun simctl launch booted io.openorcha.entangle, and xcrun simctl io booted screenshot <path>.png for a screenshot. Keep changes minimal and idiomatic SwiftUI. Work on a branch, open a PR, never merge. Hand off to Atlas for review before calling done, whether or not the task lists a review chain, and fix every numbered finding Atlas posts."
```

```
/orcha-register-agent "Android Dev" --role "Android engineer (Compose)" --prompt "You own android/ in the Entangle app. Build with: cd android && ./gradlew assembleDebug testDebugUnitTest - before opening a PR. To run the app on the booted emulator: ./gradlew installDebug, adb shell am start -n io.openorcha.entangle/.MainActivity, and adb exec-out screencap -p > <path>.png for a screenshot; to force rotation for a test, first run adb shell settings put system accelerometer_rotation 0, then set user_rotation to 1 for landscape or 0 for portrait. Keep changes minimal and idiomatic Jetpack Compose. Work on a branch, open a PR, never merge. When a task comes from a GitHub issue, post the triage comment the definition of done asks for and reference the issue in the PR body ('Fixes #<n>'). Hand off to Atlas for review before calling done, whether or not the task lists a review chain, and fix every numbered finding Atlas posts."
```

```
/orcha-register-agent "Web Dev" --role "Web engineer (Vite + TypeScript)" --prompt "You own web/ in the Entangle app. Run npm test && npm run build in web/ before opening a PR. Keep changes minimal; no new dependencies. Work on a branch, open a PR, never merge. Hand off to Atlas for review before calling done, whether or not the task lists a review chain, and fix every numbered finding Atlas posts."
```

Aliases with a space (`iOS Dev`, `Android Dev`, `Web Dev`) must be quoted in every command, exactly as above. Check the result on the portal's **Agents** page: the **Roster** lists `Atlas`, `iOS Dev`, `Android Dev`, `Web Dev`.

### 0.5 Set the gates and connect GitHub (portal)

- Top bar -> **Autonomy** -> choose **Build to PR** ("Agents execute approved plans up to an open PR; you still merge."). Do not use **Full** for the demo: under Full a finished task auto-completes and you lose the verification beat.
- Top bar -> **Notifier** must read **Running**; if it reads **Paused**, agents will not wake.
- Top right: pick **Kedar** as the acting human. The **New** task button, the **Accept** / **Reject...** buttons, and the GitHub page's **Start** button stay disabled until an acting human is selected.
- **Settings** -> **GitHub access** -> paste the token from 0.1 into **Paste a GitHub personal access token…** -> **Save token** -> **Test**. Then open the **GitHub** page (left nav): the header shows the bound repo; if it says **No repo connected**, click **Connect repo** and pick it. The **Issues** and **Pull requests** tabs should list the repo's open items (empty at `demo-start`).

### 0.6 Pair the phone (mobile)

Portal top bar -> **Pair phone** (also under **Settings** -> **Phone pairing**) shows a QR code. On the phone: open Orcha -> **Add your Orcha** -> scan the code (or **Can't scan? Enter the address**). The workspace appears under **My Orchas** with tabs **Home · Tasks · Requests · Agents · Search**. On **Home**, the repo chip (`owner/name`) with its **Hub** link opens the phone's GitHub hub (**Issues** / **Pull requests**, filters **Open** / **Mine**).

Optional: Settings on the phone -> **Needs-you alerts** on. These are local alerts from a background check ("expect minutes to an hour, not instant"), so do not promise instant push on camera.

### 0.7 Screen layout for recording

Left: the portal (**Tasks** page). Right: the Claude Code tab (for `/orcha-*` commands) with a second terminal tab running `git log --oneline --all --graph` or the web app (`cd web && npm run dev`, http://localhost:5173). For D and F, put the iPhone simulator and the Android emulator windows on the right instead - the apps launching inside them is the shot. Phone on a stand or mirrored to the Mac.

Where the live agent work is visible: portal **Agents** -> pick the agent -> the **Worker runs** card (a pulsing **live stream** label while it works); portal **Tasks** -> the card -> **Runs & diffs** and **Thread**; phone **Tasks** -> the task -> **Worker runs** / **Thread**, or **Agents** -> the agent -> **Recent runs** -> **Open work log →**.

### 0.8 What must never be on screen

Only this repo, this Orcha project, and the four agents above. Every task title, branch name, PR, issue, and chat message in the recordings is about the Entangle app. If the Mac has other Orcha projects, record inside a fresh clone with its own `orcha init` (0.3) so the board starts empty.

---

## Scenario A - "Add a feature: one task graph, three platforms" (edited length 4-5 min)

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
| 3:00 | portal | Wait. Show Atlas at work: **Agents** -> **Atlas** -> the **Worker runs** card. | "Atlas is in its own git worktree under `.orcha-worktrees/`. It can't step on anyone else." | The **live stream** label pulsing and the run log scrolling; a branch `orcha/task-...` appears in the terminal's git graph. |
| ~8:00 | portal + GitHub | Atlas's PR opens; the task flips to **Awaiting verification**. Open the card: **Result claimed by Atlas**, **Definition of done**. After CI is green, open the PR on GitHub and merge it. Return to the task and click **Accept** -> **Accept**. | "The shared contract has to land before the platform work starts. I merge it, then accept the task. Now watch the three waiting tasks." | The content PR merges. Toast `Accepted · completed`. The three platform cards move from *Waiting* to *Ready*, then to *In progress* as `iOS Dev`, `Android Dev`, and `Web Dev` wake from the updated `origin/main`. |
| ~9:00 | portal + terminal | Click between the three agents' **Worker runs** cards. Terminal: `ls .orcha-worktrees/`. | "Three agents, three platforms, three worktrees, at the same time. Same content, same definition-of-done shape, different toolchains." | Three live streams, three worktrees, three branches. |
| ~20:00 | CLI | First platform PR opens and the task hands off to Atlas. In the Claude Code tab: `/orcha-thread <that task_id>`. | "The reviewer isn't decoration. Atlas runs the same build the CI does and it can send the work back. Only when it says clean does the task come to me." | The thread printed in the terminal: the dev's hand-off, then Atlas's `clean` or numbered findings; a second commit on the PR if there were findings. |
| ~25:00 | mobile | Pick up the phone. **Home** -> **Needs you** -> **Review & decide**, or **Tasks** -> the task -> **AWAITING YOUR VERIFICATION** -> **Review & verify**. Read **Definition of done** and **Claimed result**. Tap **Approve & complete**. | "And I don't have to be at the desk for this part. Same queue, same gates, on the phone." | The card on the portal flips to *Completed* as you tap. |
| ~26:00 | portal + GitHub + app | Portal **GitHub** -> **Pull requests**: three rows, **CHECKS** green, **Merge** chip. Click one -> **Open on GitHub** and merge it there (or `gh pr merge <n> --squash --delete-branch`). Pull main, rebuild one app, open lesson 6. | "Orcha shows me the PRs and their checks, but merging is mine, on GitHub, always. Four agents, one description, three platforms. The thing I actually did was write down what done means and say yes twice." | Lesson 6 with Key takeaways bullets on screen. |

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
- Platform agents stay `pending` until you merge Atlas's content PR and click **Accept**; the status log shows `status_changed -> ready, reason: deps satisfied`. New worktrees fetch `origin/main`, so all three receive the new content contract. Each builds locally before opening its PR. Expect 8-15 minutes each, in parallel.
- Each platform task hands off to Atlas, who posts on the task thread. A `clean` verdict is normal; a finding (usually "the empty-list case is not handled") sends the task back once.
- Nothing merges on its own - Orcha has no merge button. Nothing completes without a human tap.

### Optional variant - let Atlas build the graph

If you want the orchestrator on camera doing the orchestrating: skip commands 1-4, open **Agents** -> **Atlas** and send `Add lesson 6, quantum teleportation, with a keyTakeaways list, to all three apps. Do the content yourself, then file one task per platform depending on yours, with you as reviewer.` Atlas files the same four tasks with `/orcha-task-new`. It is a stronger story and a less predictable take: check the board before continuing, and if the graph is wrong, do the Full reset and fall back to commands 1-4 (tasks cannot be deleted once created).

### Cut list for the 4-5 minute edit

Keep 0:00-2:00 in full (the promise, typed as four commands). Time-lapse the Atlas wait to ~10 s. Keep the **Accept** click and the three cards flipping in real time (the money shot, ~20 s). Time-lapse the parallel builds with the three **Worker runs** cards on screen (~20 s). Keep the `/orcha-thread` review exchange (~15 s), the phone approval (~30 s), and lesson 6 on a device (~15 s).

### Reset

```sh
# close the demo PRs and delete their branches on GitHub
for pr in $(gh pr list --state open --json number --jq '.[].number'); do gh pr close "$pr" --delete-branch; done
# local repo back to the starting point
git checkout main && git reset --hard demo-start
git worktree prune && rm -rf .orcha-worktrees
for branch in $(git for-each-ref --format='%(refname:short)' 'refs/heads/orcha/task-*'); do git branch -D "$branch"; done
# Fork only: if you merged during the take, put the fork's main back too.
git fetch origin main
git push --force-with-lease origin demo-start:main
```

In the portal, completed demo tasks can stay on the board between takes. For an empty board, wipe the demo project's Orcha data and start over (this is the demo repo's own Orcha stack, nothing else): `orcha init --force --reset-data --objective "Ship Entangle on iOS, Android, and web" --as Kedar --github <your GitHub handle>`, then re-register the cast (0.4), set the gates (0.5), and re-pair the phone (0.6). See "Full reset" at the end.

---

## Scenario B - "Fix a bug with a reviewer in the loop" (edited length ~3 min)

**Story.** A user reports that the web quiz says "out of 4" when there are five questions. `Web Dev` fixes it and opens a PR. Atlas sends it back: no regression test. `Web Dev` adds the test and hands back, Atlas says clean, and the human verifies from the phone. The point: the loop is the product, and the review step has teeth.

**The bug.** The branch `demo/quiz-score-bug` carries one commit on top of `demo-start` that makes `web/src/main.ts` print `questions.length - 1` on the result screen.

### Prerequisites

Setup done, cast registered, autonomy **Build to PR**. Plant the bug:

```sh
git checkout main && git reset --hard demo-start
git fetch upstream demo/quiz-score-bug
git merge --ff-only upstream/demo/quiz-score-bug    # main now has the bug
git push origin main                                 # agents work from the pushed main
cd web && npm run dev                                # keep this running for the reveal
```

Open http://localhost:5173/#/quiz, answer five questions, and confirm the result reads "out of 4".

### Steps

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | web app | Show the result screen with "out of 4". | "Five questions, and the app says I scored out of four. Let's file it the way a real bug gets filed." | The bug. |
| 0:20 | portal | **Tasks** -> **New**. **Title** `Quiz result says "out of 4" for a 5-question quiz`; **Description** `On web, finish the quiz: the result screen shows the total as one less than the number of questions. Fix it.`; **Definition of done** `Result screen shows "out of 5" with the current content; npm test and npm run build pass; PR open against main.`; **Assignee** `Web Dev`; expand **Protocol** -> **Review chain** `Web Dev -> Atlas -> loop until clean -> Kedar`, **Hand-off to** `Atlas`. **Create task**. | "Notice I didn't say where the bug is. And I put the reviewer in the chain again." | Toast `Task created · assigned to Web Dev`; card under *In progress*. |
| ~4:00 | portal | `Web Dev`'s PR opens; the task hands off to Atlas. | "First fix is up. It's probably right. The question is whether it's protected." | PR with a one-line diff. |
| ~7:00 | CLI | Atlas posts a finding. In the Claude Code tab: `/orcha-thread <task_id>` (the id is on the task card). | "The reviewer doesn't accept 'trust me'. Its review standard requires a regression test, even though I didn't prescribe one." | The thread in the terminal: Atlas's numbered finding, the task handed back; `Web Dev` wakes again. |
| ~11:00 | portal | Second commit lands: a small `scoreLine`-style helper plus a Vitest case. Atlas replies clean. Task flips to **Awaiting verification**. | "Second pass. Test in place. Now, and only now, it comes to me." | PR shows 2 commits; CI green. |
| ~12:00 | mobile | On the phone: **Tasks** -> the task -> **Review & verify** -> **Approve & complete**. Then merge the PR on GitHub and refresh the quiz. | "Out of five. And the next person who breaks this gets told by a test, not by a user." | Card flips to *Completed*; result screen reads "out of 5". |

### Expected agent behavior

- `Web Dev` finds the `- 1` in `web/src/main.ts` within its first minutes, fixes it, runs `npm test && npm run build`, opens the PR, hands off to Atlas via a task request (not `/orcha-done` - the chain says Atlas first).
- Atlas runs the tests, notices there is no test for the total, and sends the task back with one numbered finding.
- `Web Dev` extracts the result text into a small pure function and adds a Vitest case that asserts `out of 5` for the current content, then hands back. Atlas says clean and `Web Dev` calls `/orcha-done` -> `needs_verification`.
- Keep the DoD exactly as written: the missing-test finding comes from Atlas's standing review rule, not from a hint in the task. If `Web Dev` proactively adds a test anyway, Atlas can say clean on the first pass; record that as a shorter, still-valid review rather than manufacturing a finding.

### Cut list for the ~3 minute edit

Bug reveal (15 s), task creation (30 s), time-lapse to first PR (10 s), Atlas's finding via `/orcha-thread` (20 s), time-lapse to second commit (10 s), clean verdict + phone approval (20 s), the "out of 5" reveal (10 s).

### Reset

```sh
for pr in $(gh pr list --state open --json number --jq '.[].number'); do gh pr close "$pr" --delete-branch; done
git checkout main && git reset --hard demo-start
git fetch origin main
git push --force-with-lease origin demo-start:main   # fork only: removes the planted bug
git worktree prune && rm -rf .orcha-worktrees
for branch in $(git for-each-ref --format='%(refname:short)' 'refs/heads/orcha/task-*'); do git branch -D "$branch"; done
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

## Scenario D - "Run it: simulator + emulator, verified by a second agent" (edited length ~3 min)

**Story.** No code changes - the human wants to see the apps run and wants somebody other than the author to confirm it. Two platform agents build the apps from `main` and launch them on the iPhone simulator and the Android emulator sitting on the presenter's screen. Each hands off to Atlas, who does not take their word for it: it installs and launches the build itself, looks at the screen, and posts what it saw. The human watches the agents' live work log on the phone and approves from there.

### Prerequisites

Setup done, autonomy **Build to PR**, and - the point of this scenario - a booted iPhone simulator and a booted Android emulator, both windows visible on screen (0.1). The `iOS Dev` and `Android Dev` prompts from 0.4 already contain the install/launch recipe. Neither app should be installed yet:

```sh
xcrun simctl uninstall booted io.openorcha.entangle 2>/dev/null
adb uninstall io.openorcha.entangle 2>/dev/null
```

If your Mac has only one of the two SDKs, run the scenario with that platform alone; it works the same.

### Steps

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | Mac | Show the empty simulator and emulator side by side. | "Two empty phones. I have not built anything today. I'm going to ask two agents to build the apps and put them on these screens, and I'm going to ask a third agent to check their work before I look at it." | Simulator and emulator home screens. |
| 0:20 | CLI | Create the two run tasks (commands below). | "Notice the definition of done: installed, launched, screenshot taken, and a sentence about what's on screen. And the hand-off goes to Atlas, not to me." | Two tasks, `in_progress`, assigned to `iOS Dev` and `Android Dev`. |
| 1:00 | portal + Mac | **Agents** -> **iOS Dev** -> **Worker runs** (then **Android Dev**). Keep the simulator windows in frame. | "Their commands scroll by - xcodegen, xcodebuild, gradle - and then..." | The **live stream** log; a minute or two later the Entangle icon appears and the lessons list opens in the simulator, then in the emulator. |
| ~5:00 | mobile | On the phone: **Tasks** -> one of the run tasks -> **Worker runs**. | "Same live log on the phone. I could be on the couch." | The worker's commands streaming on the phone. |
| ~7:00 | portal | Each task hands off to Atlas. **Agents** -> **Atlas** -> **Worker runs**. | "Atlas is the second pair of eyes. It doesn't read the thread and nod; it installs the build itself and looks at the screen." | Atlas's run reinstalls and relaunches the app on the same simulator (the app blinks and reopens), takes a screenshot, and posts on the thread. |
| ~10:00 | CLI | `/orcha-thread <ios task_id>` and `/orcha-thread <android task_id>`. | "Two reports per platform: the builder's and the verifier's. They agree on what's on the screen." | The dev's hand-off message and Atlas's verification message, with the simulator name, the build commands, and what the screen showed (iOS: the five-lesson home screen; Android: the coin-flip tally). |
| ~11:00 | mobile | **Tasks** -> the task -> **Review & verify** -> **Approve & complete**, for both tasks. | "Built by one agent, checked by another, approved by a human. That's the chain, even for something this small." | Both cards flip to *Completed*; the apps are still running on both screens. |

### The two commands

```
/orcha-task-new "iOS: build Entangle from main and run it on the iPhone 16 simulator" --assign "iOS Dev" --review-chain "iOS Dev -> Atlas -> Kedar" --handoff-to Atlas --description "No code changes. Build the app from main, install and launch it on the booted iPhone 16 simulator, confirm the five-lesson home screen appears, and take a screenshot." --dod "Entangle is installed and running on the booted iPhone 16 simulator from a fresh xcodegen generate + xcodebuild of main; a screenshot of the five-lesson home screen is saved under ios/build/ (untracked); a thread message names the simulator, the exact build commands, and what the screen shows. No PR."
```

```
/orcha-task-new "Android: build Entangle from main and run it on the Pixel emulator" --assign "Android Dev" --review-chain "Android Dev -> Atlas -> Kedar" --handoff-to Atlas --description "No code changes. Build the app from main, install and launch it on the booted Android emulator, open the Entangled coin flip screen and press Measure ten times in entangled mode (adb shell input tap works), and take a screenshot." --dod "Entangle is installed and running on the booted emulator from a fresh ./gradlew installDebug of main; a screenshot is saved under android/app/build/ (untracked); a thread message names the emulator, the exact commands, and what the screen shows, including the coin-flip tally. No PR."
```

### Expected agent behavior

- `iOS Dev`: `cd ios && xcodegen generate && xcodebuild -scheme Entangle -destination 'platform=iOS Simulator,name=iPhone 16' -derivedDataPath build build`, then `xcrun simctl install booted build/Build/Products/Debug-iphonesimulator/Entangle.app`, `xcrun simctl launch booted io.openorcha.entangle`, `xcrun simctl io booted screenshot ...`. `simctl` launches and screenshots but does not provide general tap automation, so the iOS proof intentionally stops at the five-lesson home screen. First build 2-4 minutes.
- `Android Dev`: `cd android && ./gradlew installDebug`, `adb shell am start -n io.openorcha.entangle/.MainActivity`, taps its way to the coin flip with `adb shell input tap`, presses Measure, `adb exec-out screencap -p > ...`. 1-3 minutes on a warm Gradle daemon.
- Both hand off to Atlas. Atlas re-runs the install and launch itself on the same simulator/emulator, screenshots, and posts a verification message on each thread; then each dev calls `/orcha-done` -> `needs_verification`.
- Screenshots stay in the worktree (the paths are in the thread messages: `.orcha-worktrees/<task>/ios/build/...`). If an agent also attaches the PNG to its thread message (the portal's thread composer accepts images and shows them as thumbnails), that is a bonus, not something to promise on camera - the simulator windows on screen are the shot.

### Cut list for the ~3 minute edit

Empty phones (10 s), the two commands (30 s), time-lapse of the live logs until the apps appear - keep the moment the app opens in each window in real time (~30 s), the phone's **Worker runs** view (10 s), Atlas's relaunch (15 s), the two thread reports (20 s), the phone approval (20 s).

### Reset

Nothing changed in git. Quit and uninstall the apps so the next take starts from empty phones:

```sh
xcrun simctl terminate booted io.openorcha.entangle; xcrun simctl uninstall booted io.openorcha.entangle
adb shell am force-stop io.openorcha.entangle; adb uninstall io.openorcha.entangle
git worktree prune && rm -rf .orcha-worktrees
```

The two completed tasks can stay on the board.

---

## Scenario E - "Explore the code with an agent" (edited length ~2 min)

**Story.** The human is new to the codebase and asks. Atlas answers in chat with file paths and function names - and does not touch anything, because nobody asked it to. A second, narrower question goes to the platform owner as a request from the CLI, and the answer lands on the **Requests** page and on the phone. The last question sets up Scenario F.

### Prerequisites

Setup done; `Atlas` and `Android Dev` registered with the prompts from 0.4. Repo at `demo-start`.

### Steps

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | portal | **Agents** -> **Atlas**. Composer: `Walk me through the entangled coin flip: where is the logic on each platform, what do the three share, and where are the tests? Don't change anything.` -> **Send**. | "I haven't read this code. Let me ask the agent that runs the project." | Atlas's reply in about a minute. |
| 0:45 | portal | Read the reply. | "Three files, one shared content file, and it tells me where the tests are and where they aren't." | A file-level answer: `web/src/coinflip.ts` (`flipPair`, `record`, `agreementRate`), `android/.../CoinFlip.kt` (`CoinFlipModel.flipPair`, `Tally`), `ios/Entangle/CoinFlipView.swift` (`CoinFlipModel.flipPair` with an injectable random generator); the labels and disclaimer come from `content/lessons.json` -> `coinFlip`; tests in `web/src/content.test.ts` and `android/.../CoinFlipTest.kt`, none on iOS. |
| 1:30 | portal | Composer: `Which platform loses the coin-flip tally if the phone rotates, and why?` -> **Send**. | "A pointed question. Let's see if it actually reads the code or just guesses." | Atlas: Android - `CoinFlipScreen` in `MainActivity.kt` keeps `mode`, `last`, and `tally` in `remember`, which does not survive the activity being recreated on rotation (`rememberSaveable` would); iOS is portrait-only (`project.yml`), web has no rotation. |
| 2:15 | CLI | In the Claude Code tab: `/orcha-ask "Android Dev" "Which composables in android/ keep UI state with remember, and which of those would survive rotation as written? Answer only, no changes."` | "Same question to the owner of that code, as a request - so it's tracked, not lost in a chat." | The request id prints; on the portal, **Requests** shows it open, then answered by `Android Dev` a minute later, naming `EntangleApp` (`screen`), `QuizScreen` (`index`, `selected`, `score`), and `CoinFlipScreen`. |
| 3:00 | mobile | On the phone: **Requests** tab -> the request; then **Agents** -> **Atlas** -> **Converse**. | "Answers follow me too. And I now know exactly what to file." | The request and its answer; the two chat turns. |

### Expected agent behavior

- Both chat questions are answered inline and nothing is created: no task, no branch, no PR. If Atlas offers to file a fix, say "not yet".
- `/orcha-ask` creates an info request to `Android Dev`; the agent wakes, reads the code, and answers with `/orcha-respond`. The answer is visible on the portal **Requests** page and the phone's **Requests** tab.
- Facts the answers should match (verified against the repo at `demo-start`): the shared piece is the JSON copy, not shared code - each platform has its own ~40-line model; only Android and web have unit tests; all Compose state in `MainActivity.kt` uses `remember`, so rotation resets the coin-flip tally, the quiz progress, and the current screen.

### Cut list for the ~2 minute edit

First question and answer (40 s, scroll the answer slowly), second question and answer (30 s), the `/orcha-ask` command and the answered request (30 s), phone (15 s).

### Reset

Nothing to reset. **Agents** -> **Atlas** -> **End conversation** for a clean chat next take; the answered request can stay.

---

## Scenario F - "From a GitHub issue to a reviewed PR" (edited length ~4 min)

**Story.** A bug lives where bugs live: in a GitHub issue. The human files it from the CLI, then starts it from the portal's **GitHub** page (or the phone's GitHub hub) with one click, picking the agent who owns Android. Orcha creates the task and comments on the issue. `Android Dev` posts a triage comment on the issue, fixes the bug, opens a PR that references the issue, and hands off to Atlas. The human checks the PR and its CI checks without leaving Orcha, approves from the phone, merges on GitHub, and the issue closes itself.

**The bug** (real, present at `demo-start`): on Android, all screen state lives in `remember`, so rotating the device on the coin-flip screen throws you back to the lessons list and drops the tally. iOS is portrait-locked and cannot hit it; web has no rotation. Scenario E's last two answers point straight at it.

### Prerequisites

Setup done including 0.5's **GitHub access** token, autonomy **Build to PR**, the Android emulator booted with the app from `main` installed (`cd android && ./gradlew installDebug`), repo at `demo-start`. Show the bug once before recording: open **Entangled coin flip**, press **Measure** a few times, then force landscape with `adb shell settings put system accelerometer_rotation 0 && adb shell settings put system user_rotation 1` (use `user_rotation 0` to return to portrait; the emulator's side toolbar also works). You land on the lessons list.

### Steps

| Time | Surface | You do | Narration | What the audience sees |
| --- | --- | --- | --- | --- |
| 0:00 | emulator | Coin flip, three measurements, rotate. | "Rotate the phone and the app forgets where I was. Let's file it where bugs get filed." | The lessons list replaces the coin-flip screen. |
| 0:20 | CLI | File the issue (command below). Copy the issue number it prints. | "A normal GitHub issue. Nothing Orcha-specific in it." | `gh` prints the issue URL. |
| 0:45 | portal | Left nav -> **GitHub** -> **Issues** tab. Find the issue (filter chip **Open**, or type in **Filter by title or #number…**). Click the row's caret next to **Start →** (tooltip **Assign to an agent**) -> **Android Dev** (it is under **Suggested**). | "This is Orcha reading my repo. One click turns the issue into a task and hands it to the Android owner." | Toast **Task created**. A card `GH #<n>: Android: rotating the phone…` appears on **Tasks** under *In progress*, assigned to `Android Dev`. |
| 1:15 | GitHub | Open the issue in the browser (row -> **Open on GitHub**). | "And it tells GitHub. Anyone watching the repo sees that an agent picked it up, and that a human still has to verify before anything merges." | The comment `🤖 Orcha started task … — assigned to **Android Dev**. Work arrives as a PR; a human verifies before anything merges.` |
| ~3:00 | GitHub | Refresh the issue. | "First thing the agent does is triage, in public: which file, what's wrong, how it will prove the fix." | `Android Dev`'s triage comment naming `MainActivity.kt`, `remember` vs `rememberSaveable`, and the repro. |
| ~10:00 | portal | The PR opens and the task hands off to Atlas. **GitHub** -> **Pull requests** tab. Click the PR: sub-tabs **Conversation**, **Checks (N)**, **Files changed (N)**; right rail **Status**, **Reviewers**, **Checks**. | "I'm checking the PR without leaving Orcha. The checks are the same GitHub Actions the repo always runs." | The PR row with **CHECKS** filling in and the **MERGE** state chip; the diff under **Files changed**. |
| ~10:30 | CLI | `gh pr list` then `gh pr checks <pr-number>`. | "Same thing from the terminal, for the people who live there." | One open PR, three checks: `web (Vite)`, `android (Compose)`, `ios (SwiftUI)`. |
| ~12:00 | CLI | `/orcha-thread <task_id>` (the id is on the card). | "Atlas reviewed it - built it, ran the tests, and it wanted a regression test for the state that was being lost. Second pass is clean." | Atlas's finding(s) and `clean`; the task at **Awaiting verification**. |
| ~13:00 | mobile | **Home** -> the repo chip -> **Hub** -> **Pull requests** -> the PR (**PR #n**), then **Tasks** -> the task -> **Review & verify** -> **Approve & complete**. | "PR on the phone, verification on the phone." | The PR detail on the phone; the card flips to *Completed*. |
| ~14:00 | GitHub + emulator | Merge the PR on GitHub (the PR page, or `gh pr merge <n> --squash --delete-branch`). Refresh the issue. `cd android && git pull && ./gradlew installDebug`, open the coin flip, measure, rotate. | "Merged by me, on GitHub. The PR said 'Fixes #n', so the issue closed itself. And the tally survives the turn." | The issue shows **Closed** with the bot comment, the triage comment, and the linked PR; the emulator keeps the coin-flip screen and the tally in landscape. |

### The issue (copy-paste into the CLI)

```sh
gh issue create --title "Android: rotating the phone on the coin-flip screen loses the tally and jumps back to the lessons list" --body "Steps: open Entangled coin flip, press Measure a few times, rotate the phone (or the emulator).
Expected: still on the coin-flip screen, same tally.
Actual: back on the lessons list, tally gone. Same on the quiz: rotate mid-quiz and you are back at question 1.
Notes: iOS is portrait-only so it cannot hit this; web has no rotation. Keep Android rotatable - fix the state, don't lock the orientation."
```

Phone alternative for the start: **Home** -> the repo chip -> **Hub** -> **Issues** -> press and hold **Start** on the issue -> **Start with an agent…** -> **Android Dev**. (A plain tap on **Start** creates the task unassigned - then assign it on **Tasks**.)

### What Orcha creates from the issue

The task's title is `GH #<n>: <issue title>`; its description is the issue text plus the issue URL and "Triggered from the GitHub hub"; its definition of done is Orcha's standard one for issues: post a triage comment on the issue first, fix it, open a PR referencing #n, fresh-session review, then human review, never merge. There is no review chain on it - that is why the 0.4 prompts say "hand off to Atlas whether or not the task lists a review chain". Starting the same issue twice shows **Already tracked — <task id>** instead of a second task.

### Expected agent behavior

- `Android Dev` comments on the issue (`gh issue comment`), then switches `CoinFlipScreen` (and, if it is thorough, `EntangleApp`'s `screen` and `QuizScreen`'s state) to `rememberSaveable` with a small `Saver` for `Tally`/`FlipResult`, runs `./gradlew assembleDebug testDebugUnitTest`, opens the PR with `Fixes #<n>` in the body, hands off to Atlas. 8-12 minutes.
- Atlas builds it, asks for a regression test if there is none (a JVM unit test for the `Saver` round-trip is enough; instrumented rotation tests are out of scope), then says clean. `Android Dev` calls `/orcha-done` -> `needs_verification`.
- If the agent locks the orientation to portrait instead of preserving state, Atlas should send it back - the issue says not to. If Atlas lets it through, reject from the phone with that one line; the agent will redo it.

### Cut list for the ~4 minute edit

The rotate bug (15 s), `gh issue create` (15 s), the **Start →** click and the card appearing (20 s), the bot comment on GitHub (10 s), time-lapse to the triage comment (10 s) and to the PR (10 s), the **Pull requests** tab with checks (25 s), `gh pr checks` (10 s), the review thread (20 s), phone PR + approval (30 s), merge, closed issue, and the rotation surviving (30 s).

### Reset

```sh
for pr in $(gh pr list --state open --json number --jq '.[].number'); do gh pr close "$pr" --delete-branch; done
gh issue close <n> --reason "not planned"        # if the take did not merge
git checkout main && git reset --hard demo-start
git fetch origin main
git push --force-with-lease origin demo-start:main   # fork only: main is back to the bug
git worktree prune && rm -rf .orcha-worktrees
for branch in $(git for-each-ref --format='%(refname:short)' 'refs/heads/orcha/task-*'); do git branch -D "$branch"; done
adb shell settings put system user_rotation 0    # emulator back to portrait
adb shell settings put system accelerometer_rotation 1
cd android && ./gradlew installDebug             # the buggy build back on the emulator
```

The next take needs a fresh issue (a new number); the old task stays on the board unless you do the Full reset.

---

## Troubleshooting on camera

| Symptom | Fix |
| --- | --- |
| **New** / **Accept** / **Start →** disabled | Pick the acting human in the top-right of the portal. |
| `/orcha-task-new` says the assignee alias is not registered | The alias is case- and space-sensitive: `"iOS Dev"`, `"Android Dev"`, `"Web Dev"`, `Atlas`, quoted exactly as in 0.4. |
| A task sits at *Ready* and nobody wakes | Top bar **Notifier** says **Paused** - click it to **Running**. Or **Requests** -> the pending request -> **Nudge**. |
| A finished task jumped straight to *Completed* | Autonomy was **Full**. Switch to **Build to PR** and re-run the scenario. |
| Platform tasks stayed *Waiting* after Accept | They depend on a task that is not the one you accepted. Open the card and check **Depends on**. |
| The agent opened the PR against the wrong repo | `gh auth status` on the Mac must show an account with push access to the repo the clone points at. |
| A reset command would target `open-orcha/entangle` | Stop. Write scenarios belong in a disposable fork whose remote is `origin`; the canonical repo should be `upstream`. |
| The **GitHub** page says **No repo connected**, or lists nothing | **Connect repo** (top right of the page) and check **Settings** -> **GitHub access** -> **Test**. The clone's `origin` must be a github.com remote when you run `orcha init`. |
| **Start →** on the issue says **Already tracked** | The issue was started in an earlier take. File a fresh issue (new number) or do the Full reset. |
| The agent's `xcodebuild` cannot find the simulator | The name in the 0.4 prompt must match `xcrun simctl list devices available` on this Mac; the simulator must be booted for `simctl install booted`. |
| `adb: no devices` in the agent's log | Boot the emulator before the take (0.1); `adb devices` must list it. |
| iOS build fails on a fresh Mac | `sudo xcodebuild -license accept` once, then `brew install xcodegen`. |

## Full reset (between recording sessions)

```sh
git checkout main && git reset --hard demo-start
git fetch origin main
git push --force-with-lease origin demo-start:main   # disposable fork only
git worktree prune && rm -rf .orcha-worktrees
for branch in $(git for-each-ref --format='%(refname:short)' 'refs/heads/orcha/task-*'); do git branch -D "$branch"; done
for pr in $(gh pr list --state open --json number --jq '.[].number'); do gh pr close "$pr" --delete-branch; done
for issue in $(gh issue list --state open --json number --jq '.[].number'); do gh issue close "$issue" --reason "not planned"; done
xcrun simctl uninstall booted io.openorcha.entangle; adb uninstall io.openorcha.entangle
# Empty board: drops ONLY this demo project's Orcha database and creates a fresh container.
orcha init --force --reset-data --objective "Ship Entangle on iOS, Android, and web" --as Kedar --github <your GitHub handle>
```

Then re-register the cast (0.4), set the gates and the GitHub token (0.5), and re-pair the phone (0.6). If you only want to stop the stack between sessions without wiping anything, `orcha down` stops it and `orcha up` brings the same board back.
