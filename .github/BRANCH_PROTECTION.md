# Branch Protection Setup

After pushing to GitHub, configure these rules at:
**Settings → Rules → Rulesets → New ruleset**

## Main branch

- Target: `main`
- Require pull request before merging
  - Required approvals: 1
  - Dismiss stale reviews: yes
- Require status checks to pass
  - Required checks: `lint`, `test`, `build`
- Block direct pushes (no bypass)
- Require linear history

## Dev branch

- Target: `dev`
- Require status checks to pass
  - Required checks: `lint`, `test`
- Allow direct pushes from maintainers

## Release flow

1. Bump `VERSION` file on dev
2. PR from dev → main
3. Merge triggers auto-tag workflow
4. Tag push triggers release workflow
5. GitHub Release created with jar artifact
