INSERT INTO "trip_role" ("id", "name") VALUES
(gen_random_uuid(), 'Organizer'),
(gen_random_uuid(), 'Participant'),
(gen_random_uuid(), 'Viewer')
ON CONFLICT (name) DO NOTHING;