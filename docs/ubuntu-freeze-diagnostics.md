# Investigating the Ubuntu system freeze

The affected machine runs Ubuntu 22 and RuneLite through Jagex Launcher for Linux (Beta). The user confirmed on 2026-10-12 that the entire operating system stops responding shortly after login, sometimes before world entry, and requires a PC restart. This is a system lockup, not a confirmed Java crash. The exact OS/kernel, client/runtime, GPU and launcher packaging versions are still unknown. QuestCape's filesystem usage is a hypothesis; neither the root cause nor a fix is established.

Collect existing evidence first. Another forced-reboot reproduction is not required to start investigating. These are commands for the user to run on the affected Ubuntu machine; they do not run inside the plugin and do not interact with the game.

## First evidence to send

1. Approximate local time/date of the freeze and whether it happened before world entry, while loading, or after entering the world. Include whether sound continued, the mouse moved, or any remote connection still worked, if already known.
2. RuneLite `client.log`, `launcher.log` and the rotated logs covering that session, from `~/.runelite/logs`. If the launcher sandbox uses a different home, use its RuneLite logs directory. Include the startup portion so the actual runtime/client versions are visible. Do not send the whole `.runelite` directory, account/profile configuration, login tokens or authentication environment variables. Redact personal paths, player names and any credentials from the selected evidence before sharing.
3. The journal for the boot that froze, including kernel messages. The previous boot is `-1` only if the most recent restart followed that freeze. Use the boot list to choose the correct boot if there have been later restarts.
4. The machine's OS/kernel, GPU driver, RAM and filesystem information below. Also describe how Jagex Launcher and RuneLite are installed (package, AppImage, container, compatibility layer or other method), and copy the exact versions shown by their About interfaces. System `java -version` may differ from RuneLite's bundled Java; startup logs are more useful.
5. Which QuestCape version/source revision was running, whether GPU/117 HD and other third-party plugins were enabled, and whether a comparable login with QuestCape disabled is already known to work. The current local development candidate is 3.0.1; do not assume it is the version on the Ubuntu PC.

RuneLite documents the [Linux log location and client troubleshooting](https://github.com/runelite/runelite/wiki/Troubleshooting-problems-with-the-client). The systemd manual documents [boot selection and kernel journal output](https://www.freedesktop.org/software/systemd/man/255/journalctl.html).

## Collect the failed boot's journal

Run in a terminal after restarting. The output files are placed in a new, private directory under your home. Read them before attaching; system logs can contain personal paths and unrelated application details. There is no automatic upload.

```bash
freeze_dir="$(mktemp -d "$HOME/questcape-freeze.XXXXXX")"
chmod 700 "$freeze_dir"
printf 'Evidence directory: %s\n' "$freeze_dir"
journalctl --list-boots --no-pager
```

If `-1` is the boot that froze, use:

```bash
journalctl -b -1 -k --no-pager > "$freeze_dir/kernel-previous-boot.txt"
journalctl -b -1 -n 2000 --no-pager > "$freeze_dir/system-previous-boot-tail.txt"
```

Otherwise replace `-1` in both commands with the appropriate boot ID from the list. If access is denied or journalctl reports limited visibility, rerun just those journalctl commands with `sudo` if you are comfortable doing so. If no previous boot exists in the journal, report that result: persistent logging may be unavailable, or a hard lockup may not have flushed its final messages. Empty logs do not rule out a kernel/driver failure.

## Collect environment details

In the same terminal, retaining `freeze_dir`:

```bash
cat /etc/os-release > "$freeze_dir/os-release.txt"
uname -a > "$freeze_dir/kernel-version.txt"
printf '%s\n' "$XDG_SESSION_TYPE" > "$freeze_dir/desktop-session-type.txt"
lspci -nnk > "$freeze_dir/pci-and-drivers.txt"
free -h > "$freeze_dir/memory.txt"
df -h "$HOME" > "$freeze_dir/home-disk-space.txt"
findmnt -T "$HOME/.runelite" > "$freeze_dir/runelite-filesystem.txt"
```

If `.runelite` is elsewhere, substitute its real directory in the last command. Record a missing command or path rather than installing tools or changing permissions just for this initial collection. These observations help distinguish local/network/FUSE storage and disk pressure from GPU, memory and other system failures; none proves causation alone.

## Only if the machine remains remotely responsive

If a terminal or an already configured remote connection still works during a future freeze, Java thread dumps can show whether the client thread, Swing EDT or plugin worker is blocked. Do not configure remote access just for this initial request. Use a JDK compatible with the running RuneLite JVM, from the same user and process/container environment. Oracle documents [jcmd and Thread.print](https://docs.oracle.com/en/java/javase/11/tools/jcmd.html).

Find the RuneLite Java PID using `jcmd -l` or your process viewer; do not publish full process command lines because launch arguments may contain account tokens. Substitute that numeric PID below. Take three samples about five seconds apart, manually, if the system remains responsive:

```bash
runelite_pid=12345
timeout 10s jcmd "$runelite_pid" Thread.print -l > "$freeze_dir/threads-1.txt" 2>&1
```

Repeat using `threads-2.txt` and `threads-3.txt`. If attach fails, send the error instead of forcing attachment or changing security settings. This step is unavailable during a complete machine lockup and is not a prerequisite for investigating the journal evidence. Do not collect or send heap dumps or JVM environment/property dumps.

## Investigation and acceptance sequence

Review the timestamped logs first for GPU resets/hangs, out-of-memory activity, blocked storage I/O, kernel errors and JVM failures. These are candidate explanations, not findings. Correlate any relevant evidence with QuestCape activity and the exact installed revision.

Then choose a controlled comparison based on that evidence, changing one factor at a time. A known stable QuestCape-disabled login is useful; new reproductions must be user-run and weighed against the cost of another machine lockup. Plugin isolation and graphics-acceleration comparisons may help, but no blanket cache deletion, permissions rewrite or GPU change is justified yet. Preserve the original logs/cache before any later targeted intervention. A safe-mode run disables multiple plugins and is an isolation clue, not proof that QuestCape caused the failure.

A root-cause claim requires evidence linking the failing operation to the freeze and a targeted correction. Acceptance requires sustained responsiveness on the affected machine across the previously failing login point and normal login/Sync/refresh/restart use. A clean Windows build, successful JVM launch, empty error log or untested defensive change does not satisfy that gate. The final full RuneLite/Jagex compliance audit follows user acceptance, then the authorized source PR targets `master`. The Plugin Hub PR requires a separate later instruction.
