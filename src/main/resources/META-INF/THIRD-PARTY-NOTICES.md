The Java plugin code is licensed separately from guide content.

The OSRS Wiki guide fixture in `src/test/resources/fixtures/wiki-guide-15336101.html`
is an extract of https://oldschool.runescape.wiki/w/Optimal_quest_guide?oldid=15336101,
retrieved on 2026-09-12. Copyright OSRS Wiki contributors; CC BY-NC-SA 3.0:
https://creativecommons.org/licenses/by-nc-sa/3.0/ . Additional terms:
https://meta.weirdgloop.org/w/Licensing . Only the guide table was extracted;
test mutations are identified as synthetic. Runtime cached guide content retains
this attribution.

The WikiSync fixture is an anonymized, reduced response from the public
WikiSync STANDARD endpoint, captured on 2026-09-12. Its username has been
replaced with the fictional `maple_scout`; it is not a lookup of that name.
Only username, server timestamp, quests, levels and achievement diaries are kept.
It is test data, never a shipped account snapshot.

The Gradle wrapper and build structure originate from runelite/example-plugin.
Gradle is licensed under Apache License 2.0: https://www.apache.org/licenses/LICENSE-2.0 .
RuneLite and its transitive dependencies are supplied by the client, not included
in the distribution JAR. Theoatrix URLs are links; no article content is bundled.
