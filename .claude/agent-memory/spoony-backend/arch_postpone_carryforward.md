---
name: arch-postpone-carryforward
description: Mécanique de report/carry-forward des tâches (ADR-001/008), pourquoi le carry-forward paresseux est préféré au job @Scheduled, et la non-existence du log orphelin PLANNED
metadata:
  type: project
---

Carry-forward des tâches non faites (bug N5 de l'audit frontend) : choix paresseux (option B), pas de job end-of-day.

**Faits (vérifiés dans le code) :**
- `TaskLogService.createLogs`/`createManualLog` datent TOUJOURS les logs PLANNED à `today` (jamais date passée). Donc un `UserTaskLog` PLANNED daté d'hier ne peut pas exister dans l'implémentation actuelle.
- Le report est porté par `UserTask.dueDate` (décalée), pas par les logs. `TaskPostponeAdapter.postponeAllActiveTasks(userId, fromDate, toDate)` décale `dueDate<=fromDate` vers toDate et supprime les logs PLANNED de `fromDate` (ADR-001 scope `date==today`).
- Le check-in step1 resurface déjà les tâches `dueDate<today` (overdue) ; `bulkPostpone` (TaskLogService:144) recale `dueDate<=today` à demain. La source de vérité du « à faire » = `dueDate`, pas un log.

**Why:** un job @Scheduled global end-of-day est cassé en multi-fuseaux (un seul cron UTC ne correspond à la « fin de journée » de personne) et créerait soit des écritures de masse, soit des logs sur dates passées → casserait l'invariant ci-dessus et la règle J+1 (TaskLogService:104). Le carry-forward paresseux au check-in n'a pas de notion de fuseau et est déjà 90% en place.

**How to apply:** si on me demande d'implémenter N5, recommander B (documenter + éventuellement auto-postpone implicite au check-in, sans @Scheduled). Si quelqu'un veut un @Scheduled, exiger un scope par fuseau utilisateur et garder l'invariant « pas de log PLANNED sur date passée », sinon la dette du log orphelin devient réelle.
