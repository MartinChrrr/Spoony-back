-- ============================================
-- V2 : Donnees d'amorcage (seed)
-- ============================================

-- ============================================
-- BASE_TASKS (21 taches reparties sur 7 categories)
-- ============================================

-- HYGIENE (3)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'tasks.hygiene.shower', 2, 'MEDIUM', 'HYGIENE', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000002', 'tasks.hygiene.brush_teeth', 1, 'MEDIUM', 'HYGIENE', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000003', 'tasks.hygiene.hair', 2, 'LOW', 'HYGIENE', NOW(), NOW());

-- MENAGE (4)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'tasks.household.groceries', 3, 'MEDIUM', 'MENAGE', NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000002', 'tasks.household.laundry', 3, 'MEDIUM', 'MENAGE', NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000003', 'tasks.household.dishes', 2, 'MEDIUM', 'MENAGE', NOW(), NOW()),
    ('b0000000-0000-0000-0000-000000000004', 'tasks.household.vacuum', 3, 'LOW', 'MENAGE', NOW(), NOW());

-- ALIMENTATION (2)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'tasks.food.cook_meal', 3, 'HIGH', 'ALIMENTATION', NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000002', 'tasks.food.prepare_snack', 1, 'LOW', 'ALIMENTATION', NOW(), NOW());

-- ADMINISTRATIF (3)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'tasks.admin.bills', 2, 'HIGH', 'ADMINISTRATIF', NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000002', 'tasks.admin.emails', 2, 'MEDIUM', 'ADMINISTRATIF', NOW(), NOW()),
    ('d0000000-0000-0000-0000-000000000003', 'tasks.admin.appointment', 2, 'HIGH', 'ADMINISTRATIF', NOW(), NOW());

-- SOCIAL (2)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'tasks.social.call_friend', 2, 'LOW', 'SOCIAL', NOW(), NOW()),
    ('e0000000-0000-0000-0000-000000000002', 'tasks.social.text_family', 1, 'LOW', 'SOCIAL', NOW(), NOW());

-- SANTE (3)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000001', 'tasks.health.take_meds', 1, 'HIGH', 'SANTE', NOW(), NOW()),
    ('f0000000-0000-0000-0000-000000000002', 'tasks.health.exercise', 3, 'MEDIUM', 'SANTE', NOW(), NOW()),
    ('f0000000-0000-0000-0000-000000000003', 'tasks.health.rest', 1, 'HIGH', 'SANTE', NOW(), NOW());

-- LOISIR (3)
INSERT INTO base_tasks (id, task_key, spoon_cost, importance, category, created_at, updated_at) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'tasks.leisure.read', 2, 'LOW', 'LOISIR', NOW(), NOW()),
    ('a1000000-0000-0000-0000-000000000002', 'tasks.leisure.music', 1, 'LOW', 'LOISIR', NOW(), NOW()),
    ('a1000000-0000-0000-0000-000000000003', 'tasks.leisure.hobby', 3, 'LOW', 'LOISIR', NOW(), NOW());

-- ============================================
-- BENEVOLENT_MESSAGES (7 contextes, 15 messages)
-- ============================================

-- WELCOME (2)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000001', 'WELCOME', 'messages.welcome.return', NOW(), NOW()),
    ('10000000-0000-0000-0000-000000000002', 'WELCOME', 'messages.welcome.no_rush', NOW(), NOW());

-- REST (2)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('20000000-0000-0000-0000-000000000001', 'REST', 'messages.rest.listen_body', NOW(), NOW()),
    ('20000000-0000-0000-0000-000000000002', 'REST', 'messages.rest.deserved', NOW(), NOW());

-- COMPLETION (2)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('30000000-0000-0000-0000-000000000001', 'COMPLETION', 'messages.completion.celebrate', NOW(), NOW()),
    ('30000000-0000-0000-0000-000000000002', 'COMPLETION', 'messages.completion.proud', NOW(), NOW());

-- LOW_ENERGY (2)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('40000000-0000-0000-0000-000000000001', 'LOW_ENERGY', 'messages.low_energy.breathe', NOW(), NOW()),
    ('40000000-0000-0000-0000-000000000002', 'LOW_ENERGY', 'messages.low_energy.gentle', NOW(), NOW());

-- ZERO_ENERGY (3)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('50000000-0000-0000-0000-000000000001', 'ZERO_ENERGY', 'messages.zero_energy.comfort', NOW(), NOW()),
    ('50000000-0000-0000-0000-000000000002', 'ZERO_ENERGY', 'messages.zero_energy.reassurance', NOW(), NOW()),
    ('50000000-0000-0000-0000-000000000003', 'ZERO_ENERGY', 'messages.zero_energy.self_care', NOW(), NOW());

-- SKIP (2)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('60000000-0000-0000-0000-000000000001', 'SKIP', 'messages.skip.ok', NOW(), NOW()),
    ('60000000-0000-0000-0000-000000000002', 'SKIP', 'messages.skip.choice', NOW(), NOW());

-- END_OF_DAY (2)
INSERT INTO benevolent_messages (id, context, message_key, created_at, updated_at) VALUES
    ('70000000-0000-0000-0000-000000000001', 'END_OF_DAY', 'messages.end_of_day.enough', NOW(), NOW()),
    ('70000000-0000-0000-0000-000000000002', 'END_OF_DAY', 'messages.end_of_day.tomorrow', NOW(), NOW());
