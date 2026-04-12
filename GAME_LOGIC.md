# Quest of the Round Table — Game Logic Reference

> **How to read this doc:**
> - Plain text = official game rules
> - `⚠️ Code differs:` = where the implementation does not match the official rules

---

## OBJECT OF THE GAME

Be the first player to become a **Knight of the Round Table**.

**Progression path:**
```
Squire → Knight → Champion Knight → Knight of the Round Table (WIN)
```

**Shield requirements:**
| Promotion | Shields needed |
|---|---|
| Squire → Knight | 5 |
| Knight → Champion Knight | 7 |
| Champion Knight → Knight of the Round Table | 10 |

Multiple players can declare victory simultaneously in the same turn.

---

## COMPONENTS

### Adventure Deck — 125 cards

#### Weapons (49)
| Card | Count | Battle Points |
|---|---|---|
| Excalibur | 2 | +20 |
| Lance | 6 | +10 |
| Sword | 16 | +10 |
| Horse | 11 | +10 |
| Battle-ax | 8 | +8 |
| Dagger | 6 | +3 |

> `⚠️ Code differs:` The code uses Excalibur=30, Lance=20, Battle-ax=15, Dagger=5 — these do not match the official rule card values above.

#### Foes (50)
| Card | Count |
|---|---|
| Dragon | 1 |
| Giant | 2 |
| Mordred | 4 |
| Green Knight | 2 |
| Black Knight | 3 |
| Evil Knight | 6 |
| Saxon Knight | 8 |
| Robber Knight | 7 |
| Saxons | 5 |
| Boar | 4 |
| Thieves | 8 |

Each Foe has **two BP values** (e.g. "15/25"):
- **Lower value** — used when the Foe is not the "named" (special) Foe for this quest
- **Higher value** — used when the Foe matches the quest's Special Foe designation

#### Test Cards (8)
| Card | Count |
|---|---|
| Test of Valor | 2 |
| Test of Temptation | 2 |
| Test of Morgan Le Fey | 2 |
| Test of the Questing Beast | 2 |

#### Allies (10)
Sir Galahad, Sir Lancelot, King Arthur, Sir Tristan, Sir Pellinore, Sir Gawain, Sir Percival, Queen Guinevere, Queen Iseult, Merlin

#### Amour Cards (8)
Various named ladies-in-waiting. Max **1 per Quest or Tournament**.

---

### Story Deck — 28 cards

#### Quest Cards (13)
| Name | Count | Stages |
|---|---|---|
| Search for the Holy Grail | 1 | 5 |
| Test of the Green Knight | 1 | 4 |
| Search for the Questing Beast | 1 | 4 |
| Defend the Queen's Honor | 1 | 4 |
| Rescue the Fair Maiden | 1 | 3 |
| Journey Through the Enchanted Forest | 1 | 3 |
| Vanquish King Arthur's Enemies | 2 | 3 |
| Slay the Dragon | 1 | 3 |
| Boar Hunt | 2 | 2 |
| Repel the Saxon Raiders | 2 | 2 |

#### Tournament Cards (4)
| Name | Count |
|---|---|
| Tournament at Camelot | 1 |
| Tournament at Orkney | 1 |
| Tournament at Tintagel | 1 |
| Tournament at York | 1 |

#### Event Cards (11)
| Name | Count |
|---|---|
| King's Recognition | 2 |
| Queen's Favor | 2 |
| Court Called to Camelot | 2 |
| Pox | 1 |
| Plague | 1 |
| Chivalrous Deed | 1 |
| Prosperity Throughout the Realm | 1 |
| King's Call to Arms | 1 |

---

## SETUP

1. Give each player 1 Squire rank card and a set of 10 shields
2. Shuffle Adventure Deck; deal **12 cards** to each player
3. Shuffle Story Deck; place face-down in the center

**Starting state per player:**
- Hand: 12 Adventure Cards
- Rank: Squire
- Battle Points: 5 (Squire base)
- Shields on rank card: 0

---

## BATTLE POINTS

### Formula
```
Total BP = Rank BP + Weapons played + Allies in play + Amour (if active)
```

### Rank BP
| Rank | BP |
|---|---|
| Squire | 5 |
| Knight | 10 |
| Champion Knight | 20 |

### Weapon BP
See Weapons table above. Multiple weapons can be played per stage — no two of the **same type** per stage.

### Ally BP
Allies are permanent once played — they remain in play across all future quests and tournaments until removed. Each ally adds its BP to every battle.

### Amour BP
Max 1 Amour card active per Quest or Tournament. Provides BP for the duration. Discarded at the **end** of the Quest or Tournament.

> `⚠️ Code differs:` The code discards Amour when a player **loses a stage**, not at the end of the full quest.

---

## SHIELDS & RANK ADVANCEMENT

### Overflow Shields
Shields earned beyond what is needed to advance carry over to the next rank.

**Example:** Squire with 6 shields earned → advances to Knight with 1 shield (6 − 5 = 1 carried over).

### Earning Shields
| Source | Amount |
|---|---|
| Quest (completed all stages) | = number of stages |
| Tournament (winner) | = number of entrants + bonus on card |
| Event Cards | as specified on card |

---

## TURN STRUCTURE

1. Active player draws the top Story Card
2. Resolve based on card type (Quest / Tournament / Event)
3. Discard used Story Card
4. Turn passes clockwise

**Hand size limit: 12 cards.** If drawing would exceed 12, player must immediately discard or play cards to return to exactly 12.

---

## QUEST FLOW

### 1. Sponsorship
- Starting with the player who drew the card, each player clockwise is asked: **"Do you want to sponsor?"**
- First to accept is the **Sponsor** — they set up stages but do **not** compete
- If all decline, the quest card is discarded and the turn ends

### 2. Sponsor Builds Stages

For each stage the sponsor places from their hand:

**Required:** 1 Foe Card **OR** 1 Test Card (never both in the same stage)
**Optional:** Any number of Weapon Cards added to the Foe/Test to raise BP

**Rules:**
- No two Weapons of the **same type** in the same stage (e.g. two Swords is illegal; Sword + Lance is fine)
- Only **1 Test Card** total across the entire quest
- Each non-test stage must have **strictly more BP** than the previous non-test stage
- Allies and Amours may **not** be placed in stages

**Card type restrictions for sponsor:**
| Type | Allowed | Limit |
|---|---|---|
| Foe | ✅ | Exactly 1 per stage |
| Test | ✅ | Max 1 across entire quest |
| Weapon | ✅ | No duplicate types per stage |
| Ally | ❌ | — |
| Amour | ❌ | — |

### 3. Players Decide to Join
- Each non-sponsor player (clockwise) decides: **join** or **sit out**
- If nobody joins, the quest is abandoned
- Sponsor deals **1 Adventure Card** from the deck to each joining player before the quest begins

### 4. Stage Resolution (Foe Stage)

1. Sponsor announces: "Foe stage" (Test is face-down until players commit)
2. Players secretly choose which cards to play (Weapon, Ally, Amour)
3. All reveal simultaneously
4. Sponsor reveals the Foe and announces whether it is **named** (higher BP) or **unnamed** (lower BP)
5. Compare each player's total BP against the Foe's BP
6. Players whose BP **does not exceed** the Foe's BP are eliminated from the quest
7. Weapon cards are discarded after the stage; Allies and Amours remain

**Card type restrictions for players (quest stage):**
| Type | Allowed | Limit |
|---|---|---|
| Foe | ❌ | Never |
| Test | ❌ | Never |
| Weapon | ✅ | No duplicate types per stage |
| Ally | ✅ | Goes to In-Play permanently |
| Amour | ✅ | Max 1 active; stays for whole quest |

> `⚠️ Code differs:` In the code, players pre-select cards for **all stages at once** before fighting begins. The official rules have players choose per-stage **before** the Foe is revealed.

### 5. Stage Resolution (Test Stage)

1. Sponsor reveals the Test Card
2. Bidding begins with the player to the sponsor's left:
   - Each player bids a number (how many Adventure Cards they will discard to pass)
   - Each subsequent bid must be **higher** than the last
   - Players may drop out at any time (they are eliminated from the quest)
3. Last remaining bidder wins the test
4. Winner discards cards equal to their bid **minus any free bids** from Ally/Amour cards
5. **Minimum bid: 3 cards** (if only 1 player remains, they must bid at least 3 or drop out)

**Free bids:**  
Queen Guinevere in play = 3 free bids → winner discards (bid − 3) cards, minimum 0.

> `⚠️ Code differs:` The code asks each player for a single bid number and picks the highest, rather than the proper escalating-bid-around-the-table mechanic.

### 6. Quest End

**Success:** Players who survived all stages each earn shields equal to the number of stages.

**Failure:** Quest ends with no shields if all adventurers are eliminated.

**Card cleanup:**
- All Foe, Weapon, and Test cards used in stages are discarded
- Sponsor draws back cards: (cards used in stages) + (number of stages)
- Ally Cards **remain in play** permanently
- Amour Card discarded **after** the quest

---

## TOURNAMENT FLOW

### 1. Entry
- Each player independently decides to enter or sit out
- If 0 players enter: skip, discard card
- If 1 player enters: automatically wins 1 shield + any bonus shields

### 2. Round 1
- Each entering player draws **1 Adventure Card**
- Each player secretly selects cards (Weapons, Allies, Amour) — same type restrictions as quest
- Simultaneous reveal; highest total BP wins

**Card restrictions:**
| Type | Allowed |
|---|---|
| Foe | ❌ |
| Test | ❌ |
| Weapon | ✅ (no duplicate types) |
| Ally | ✅ |
| Amour | ✅ (max 1) |

**Prizes:**
- Winner earns shields = number of entrants + bonus shields on the tournament card

### 3. Tie-Breaking
- If two or more players tie in Round 1 → only tied players play **Round 2**
- Round 2 is identical to Round 1 (draw 1 new card, select, reveal)
- If still tied after Round 2 → **all tied players share the win**

> `⚠️ Code differs:` The code implements Round 2 for ties, which matches the rule. However, the prize calculation (shields = entrants + bonus) may differ from what is coded.

### 4. Cleanup
- All Weapon Cards discarded; Ally Cards and Amour Cards remain in play
- Turn passes to the left of the player who drew the card

---

## EVENT CARDS

Events resolve immediately when drawn, before the next turn.

| Card | Effect |
|---|---|
| King's Recognition | Next player(s) to complete a Quest receive 2 extra shields |
| Queen's Favor | Lowest-ranked player(s) immediately draw 2 Adventure Cards |
| Court Called to Camelot | All Allies currently In-Play are discarded |
| Pox | All players **except the drawer** lose 1 shield |
| Plague | The **drawer** loses 2 shields (min 0) |
| Chivalrous Deed | Player(s) with both the lowest rank AND fewest shields gain 3 shields |
| Prosperity Throughout the Realm | All players draw 2 Adventure Cards |
| King's Call to Arms | Highest-ranked player(s) must discard 1 Weapon; if unable, discard 2 Foe cards |

> `⚠️ Code differs:` The implemented event effects may not exactly match all of the above. In particular, Pox (all others lose 1 shield) and the full King's Recognition / Queen's Favor / Court Called to Camelot effects should be verified against the code.

---

## MORDRED — SPECIAL FOE RULES

When a stage contains a **Mordred** Foe card, the following special effect applies:

- The sponsor (or the player using Mordred) may name **one Ally Card** currently in any player's In-Play area
- That Ally Card is immediately removed from play and placed in the Adventure discard pile
- This happens in addition to the normal stage fight

> `⚠️ Code differs:` The code gives the **sponsor** the ability to remove an ally from a target player after the Mordred stage. This matches the spirit of the rule.

---

## ALLY CARDS — KEY RULES

1. Can be played **at any time** (not only during quests/tournaments)
2. Once played, they are **permanently in play** — they add their BP to every future quest and tournament
3. Only removed by:
   - An Event Card that discards allies (e.g. Court Called to Camelot)
   - The Mordred foe special effect
4. Multiple Ally Cards can be in play simultaneously

---

## AMOUR CARDS — KEY RULES

1. **Max 1 Amour** active per Quest or Tournament
2. Provides Battle Points for the full duration of that Quest or Tournament
3. Placed face-up next to rank card while active
4. Discarded at the **end** of the Quest or Tournament (not mid-quest on a stage loss)
5. Some Amour Cards provide **free bids** for Test stages

---

## WINNING THE GAME

- First player to earn 10 shields as Champion Knight advances to **Knight of the Round Table** and declares victory
- If multiple players reach this in the **same turn**, they all participate in a **final Tournament**
- The winner of the final Tournament is the sole winner
- If the final Tournament is also tied, all tied players are joint winners

---

## KEY RULES QUICK REFERENCE

| Rule | Value |
|---|---|
| Starting hand size | 12 cards |
| Max hand size | 12 cards |
| Shields: Squire → Knight | 5 |
| Shields: Knight → Champion | 7 |
| Shields: Champion → Round Table | 10 |
| Shields from quest | = number of stages |
| Max Amour per Quest/Tournament | 1 |
| Max Test cards per Quest | 1 |
| Minimum bid in Test stage | 3 cards |
| Queen Guinevere free bids | 3 |
| Weapon duplicate rule | No two of same type per stage |
| Ally permanence | Until removed by Event or Mordred |

---

## IMPLEMENTATION GAPS SUMMARY

| Rule | Status |
|---|---|
| Weapon BP values (Excalibur=20, Lance=10, Battle-ax=8, Dagger=3) | `⚠️ Code uses higher values` |
| Amour discarded at quest/tournament end (not on stage loss) | `⚠️ Code discards on stage loss` |
| Per-stage card selection revealed before Foe (not pre-committed for all stages) | `⚠️ Code pre-selects all stages` |
| Escalating bid mechanic in Test stages | `⚠️ Code uses single-round bid` |
| Tournament prize = entrants + bonus | Needs verification |
| Final Tournament on simultaneous victory | Not implemented |
| Pox: all players except drawer lose 1 shield | Needs verification |
| Court Called to Camelot: discard all Allies in play | Needs verification |
| King's Recognition: 2 extra shields on next quest completion | Needs verification |
