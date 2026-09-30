# Instructions for coding agents

## Test integrity

Do not modify, delete, rename, disable, skip, weaken, or replace tests without
the repository owner's explicit approval for that specific change in the
current conversation or task.

This restriction includes:

- files under `src/test/`, `infra/tests/`, and `infra-ec2/tests/`;
- test fixtures and test-specific configuration;
- assertions, snapshots, mocks, test data, and expected values;
- coverage rules and thresholds in `pom.xml`;
- CI steps, commands, or filters that determine which tests run.

Adding a new test or changing an existing test to support an implementation
also requires explicit approval. If a production change appears to require a
test change, stop and explain the required test change before editing it.

Agents may run tests and inspect test files without additional approval.
