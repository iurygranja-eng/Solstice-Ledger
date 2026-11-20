---
name: Solstice Ledger
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#bdc8d1'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#87929a'
  outline-variant: '#3e484f'
  surface-tint: '#7bd0ff'
  primary: '#8ed5ff'
  on-primary: '#00354a'
  primary-container: '#38bdf8'
  on-primary-container: '#004965'
  inverse-primary: '#00668a'
  secondary: '#ffafd3'
  on-secondary: '#620040'
  secondary-container: '#85145a'
  on-secondary-container: '#ff93c8'
  tertiary: '#cac8ff'
  on-tertiary: '#272377'
  tertiary-container: '#aba9ff'
  on-tertiary-container: '#3c398c'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#c4e7ff'
  primary-fixed-dim: '#7bd0ff'
  on-primary-fixed: '#001e2c'
  on-primary-fixed-variant: '#004c69'
  secondary-fixed: '#ffd8e7'
  secondary-fixed-dim: '#ffafd3'
  on-secondary-fixed: '#3d0026'
  on-secondary-fixed-variant: '#85145a'
  tertiary-fixed: '#e2dfff'
  tertiary-fixed-dim: '#c3c0ff'
  on-tertiary-fixed: '#100563'
  on-tertiary-fixed-variant: '#3e3c8f'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  display-hero:
    fontFamily: Space Grotesk
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
  display-hero-mobile:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Space Grotesk
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-sm:
    fontFamily: Space Grotesk
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-code:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  stat-currency:
    fontFamily: JetBrains Mono
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  stat-currency-sm:
    fontFamily: JetBrains Mono
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system channels the nostalgic craftsmanship of modern 16-bit turn-based RPGs, specifically invoking celestial, twilight fantasy aesthetics. Engineered for both individuals and co-op couples managing shared vaults, it reframes budgeting, savings quests, and cash-flow monitoring into a cooperative party journey. 

The aesthetic is a calibrated hybrid: **Retro-Chic RPG meets Modern Functional Utility**. Crisp, high-readability contemporary typography anchors critical balance numbers, while atmospheric dialog borders, beveled parchment-styled frames, and luminous celestial glows celebrate micro-achievements. The emotional response balances tranquil cosmic focus with the tactile, rewarding feedback loop of leveling up shared financial goals.

## Colors

The palette establishes an immersive dark fantasy space where depth is created through cosmic indigo gradients rather than neutral grays.

- **Primary (`#38BDF8` - Lunar Cyan)**: Represents the lunar archetype, clear intellect, calm balances, and active interactions.
- **Secondary (`#F472B6` - Solar Magenta)**: Represents the solar archetype, companion goals, partner sync items, and XP surge moments.
- **Tertiary (`#312E81` - Cosmic Indigo)**: Forms middle-ground containers, dialogue frame backdrops, and active card segments.
- **Neutral (`#0F172A` - Midnight Base)**: Deep void canvas ensuring peak contrast for luminous retro accents.
- **Surface Elevation (`#1E1B4B` - Astral Surface)**: Standard tile surface for containers and status dialogue frames.

### Semantic Gauges
- **Emerald Green (`#34D399`)**: Positive cash flow, safe reserves, and completed campaign bounties.
- **Solar Amber (`#FBBF24`)**: Burn-rate warnings, approaching thresholds, and caution state.
- **Crimson Red (`#F87171`)**: Over-budget alerts, critical deficits, and quest failure risks.

## Typography

Typography unifies modern ergonomic reading with mechanical RPG accuracy:
- **Space Grotesk** commands headlines and quest milestones, carrying slight geometric eccentricities reminiscent of cosmic exploration.
- **Manrope** powers body copy, couple notes, and descriptions, maintaining pristine legibility on small screens.
- **JetBrains Mono** handles all transactional digits, ledger rows, timestamps, and stat meters, evoking an authentic 16-bit readout style with strict tabular numeral alignment.

## Layout & Spacing

A mobile-first 4-column layout adapts seamlessly up to an 8-column layout on tablets and a 12-column layout on desktop (max width: 1280px). Spacing strictly follows an 8pt base grid (`0.5rem` = 8px).

- **Mobile (<768px)**: 4 columns, `1rem` margins, stacked RPG party split cards (Player 1 vs Player 2 balances).
- **Tablet (768px - 1024px)**: 8 columns, `1.5rem` gutters, side-by-side companion gauges and shared campaign charts.
- **Desktop (>1024px)**: 12 columns, `1.5rem` gutters, `2.5rem` outer canvas padding, persistent party HUD rail on the left, interactive ledger center, and shared bounty vault on the right.

## Elevation & Depth

Visual hierarchy uses physical JRPG UI motifs merged with modern luminescence:

- **16-Bit Dialogue Bevel**: Containers use a 2px semi-transparent border (`rgba(129, 140, 248, 0.4)`) combined with an inset border highlight (`inset 0 1px 0 rgba(255, 255, 255, 0.15)`) and an inner glow (`inset 0 0 12px rgba(99, 102, 241, 0.15)`).
- **Background Stratification**: 
  - Base floor: Pure Midnight `#0F172A`.
  - Surface cards: Cosmic Indigo `#1E1B4B`.
  - Floated dialogues & modals: Deep Indigo `#312E81` with backdrop blur of 12px.
- **Solar-Lunar Resonance**: Primary actions and active interactive states cast a colored ambient drop-shadow (`0 4px 20px -2px rgba(56, 189, 248, 0.35)` for Lunar; `0 4px 20px -2px rgba(244, 114, 182, 0.35)` for Solar).

## Shapes

The design uses soft, deliberate radii (`roundedness: 1` / 4px base, 8px on larger surfaces). This preserves a crisp, pixel-adjacent box contour while avoiding harsh corners on touch interfaces. Modals, dialogue boxes, and stat containers simulate vintage dialog windows with structured corners and defined borders.

## Components

### Buttons & Interactive Controls
- **Lunar Core CTA (Primary)**: Solid gradient fill from `#38BDF8` to `#0284C7`, text in `#0F172A` bold. Border: 1px solid `#BAE6FD`. Pressed state: `translate-y-[1px]` with cyan shadow collapse.
- **Solar Companion CTA (Secondary)**: Solid gradient fill from `#F472B6` to `#DB2777`, white text, soft magenta ambient glow.
- **Inventory/Subtle Action**: `#1E1B4B` surface with 2px bevel border (`border-indigo-400/40`), white text, hover lighting shift to `#312E81`.

### RPG Dialogue Cards & Vault Tiles
- Background `#1E1B4B`, double-layer bevel framing, top-right or corner mini diamond glyph markers (`◆`). 
- **Party Balance Split**: Two-column breakdown displaying Partner A (Cyan accent) and Partner B (Magenta accent) with an integrated XP bar showing joint monthly budget adherence.

### Chips & Stat Badges
- Pill-compact but flat-edged with `rounded-sm`. 
- Positive (`#34D399/15` bg, `#34D399` text, `border border-emerald-500/30`).
- Caution/Burn (`#FBBF24/15` bg, `#FBBF24` text, `border border-amber-500/30`).
- Deficit/Risk (`#F87171/15` bg, `#F87171` text, `border border-rose-500/30`).

### Checkboxes & Toggle Switches
- Checkboxes: 18x18px squared box, dark background, 2px beveled border. Active state features a glowing cyan pixel checkmark.
- Dual-Partner Switch: Segmented pill showing sun and moon icons, highlighting current active attribution (Individual vs Couple).

### Input Fields
- Deep inset background (`#0F172A`), border: 1.5px solid `rgba(99, 102, 241, 0.5)`. Typography set in `JetBrains Mono` for rapid numeric entry. Focused state features an inner cyan ray glow (`box-shadow: inset 0 0 6px rgba(56, 189, 248, 0.3)`).

### Gamified Progress Meters (HP / MP / EXP Bars)
- Striped, segmented horizontal track for budget burn and savings quests:
  - Base track: `#0F172A` with beveled edges.
  - Fill: Vibrant linear gradient with 45-degree micro-striping overlay to simulate pixel art gauge meters.