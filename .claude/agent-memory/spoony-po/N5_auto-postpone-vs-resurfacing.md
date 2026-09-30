---
name: n5-auto-postpone-vs-resurfacing
description: Arbitrage N5 — pas de job de report auto nocturne ; le resurfacing au check-in couvre l'intention de la spec
metadata:
  type: project
---

N5 « report auto en fin de journée » clos comme COUVERT fonctionnellement par le resurfacing au check-in ①. Pas de job end-of-day.

**Why:** Un job nocturne qui réécrit silencieusement les dueDate retire l'agentivité à un utilisateur fragile et crée la confusion (« pourquoi ma date a changé toute seule ? »). Le check-in présente les tâches d'hier et laisse une décision consciente en un geste (reprendre / reporter via bulkPostpone ADR-008 / se reposer) = « une décision à la fois, sans culpabilité ». Le mot « automatiquement » de la spec = resurfacing garanti, pas job sans interaction.

**How to apply:** Reformuler le PRD : « Reportée automatiquement » → « resurface au check-in du lendemain, reprise ou report en un geste ». Code concerné : `spoony-frontend/app/checkin/step1.tsx` (filtre `dueDate < today`). Ajout retenu : sous-titre doux `checkin.step1Subtitle` rendant explicite l'origine « jours précédents », sans compte de tâches ni mot « en retard / overdue » à l'écran (registre « à reprendre »). Voir [[spoony-bienveillance-vs-automatisme]].
