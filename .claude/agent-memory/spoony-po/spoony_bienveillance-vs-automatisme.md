---
name: spoony-bienveillance-vs-automatisme
description: Principe d'arbitrage récurrent — préférer une décision consciente de l'utilisateur à un automatisme silencieux
metadata:
  type: feedback
---

Quand une feature peut être soit automatisée silencieusement, soit présentée comme une décision à un geste au moment du check-in, Spoony choisit la décision consciente.

**Why:** L'automatisme silencieux (job nocturne, réécriture de dates en fond) retire le sentiment de contrôle à un utilisateur fragile et crée de la confusion. La bienveillance Spoony = alléger la décision (un geste, pas de culpabilité), pas la supprimer. Confirmé sur N5 (report auto refusé au profit du resurfacing au check-in).

**How to apply:** Face à une demande de comportement « automatique » dans la spec, vérifier si l'intention réelle est « ne rien perdre / garantir le resurfacing » plutôt que « agir sans l'utilisateur ». Privilégier la 2e lecture. Refuser les jobs planifiés qui modifient les données de l'utilisateur sans interaction visible. Voir [[n5-auto-postpone-vs-resurfacing]].
