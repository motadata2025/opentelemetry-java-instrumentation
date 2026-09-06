# GitHub Actions Workflows Guide

This document describes the GitHub Actions CI/CD pipelines for building and publishing the Motadata Java Agent.

## Overview

Three workflows automate the build, test, and release process:

1. **CI** (`ci.yml`) - Quick checks on pull requests
2. **Build** (`build-javaagent.yml`) - Complete build and artifact creation
3. **Publish** (`publish-packages.yml`) - Publish to GitHub Packages

---

## 1. CI Workflow

**File**: `.github/workflows/ci.yml`

### Triggers
- Pull requests to `main` or `motadata-dev`
- Pushes to `motadata-dev` (service detector module changes)
- Manual trigger via GitHub UI

### What It Does
- ✅ Runs all 26 unit tests
- ✅ Performs code quality checks (spotless)
- ✅ Runs compiler checks (error-prone)
- ✅ Comments on PR with results
- ✅ Publishes test results to GitHub

### Usage
```bash
# Triggered automatically on PR
# Or manually from GitHub Actions tab
```

### Runtime
⏱️ ~2-3 minutes

---

## 2. Build Workflow

**File**: `.github/workflows/build-javaagent.yml`

### Triggers
- Pushes to `main` or `motadata-dev` (javaagent or service detector changes)
- Pull requests to `main` or `motadata-dev`
- Manual trigger via GitHub UI (workflow_dispatch)

### What It Does
1. ✅ Checks out code
2. ✅ Sets up Java 11
3. ✅ Builds service detector module
4. ✅ Runs all tests (26 cases)
5. ✅ Builds complete Java agent JAR
6. ✅ Uploads artifacts to GitHub
7. ✅ Generates build summary

### Outputs

**Artifacts** (available for 90 days):
- `motadata-javaagent.jar` - Complete Java agent (~100+ MB)
- `opentelemetry-resources-*.jar` - Service detector module (~12 KB)

**Build Summary**:
Shows file names, sizes, and paths in workflow summary.

### Download Artifacts
1. Go to GitHub repo → Actions tab
2. Click on workflow run
3. Scroll to "Artifacts" section
4. Download `motadata-javaagent` or `motadata-service-detector`

### Runtime
⏱️ ~10-15 minutes (builds full javaagent with all instrumentations)

---

## 3. Publish Workflow

**File**: `.github/workflows/publish-packages.yml`

### Triggers
- GitHub Release created
- Manual trigger with version input

### What It Does
1. ✅ Builds Java agent
2. ✅ Builds service detector
3. ✅ Publishes to GitHub Packages
4. ✅ Generates publish summary

### Prerequisites
- `GITHUB_TOKEN` (automatic, no setup needed)
- Gradle configured for GitHub Packages publishing

### Usage

#### Option A: Publish via Release
```bash
# Create a release on GitHub
# Workflow automatically publishes artifacts
```

#### Option B: Manual Publish
```bash
# GitHub Actions tab → publish-packages
# Click "Run workflow"
# Select branch (usually main)
```

### Runtime
⏱️ ~15-20 minutes

---

## 4. Manual Workflow Triggers

### Build Java Agent Only
```bash
# Via GitHub UI:
1. Go to Actions tab
2. Select "Build Motadata Java Agent"
3. Click "Run workflow"
4. Choose branch (main or motadata-dev)
5. Click "Run workflow"
```

### Create Release with Assets
```bash
# Via GitHub UI:
1. Go to Actions tab
2. Select "Build Motadata Java Agent"
3. Click "Run workflow" → Configure
4. Set "release" input to "true"
5. Click "Run workflow"

# OR use workflow_dispatch:
# Automatically creates release with artifacts
```

---

## 5. Artifact Management

### Access Artifacts
1. **GitHub Actions Tab**
   - Click workflow run
   - Scroll to "Artifacts"
   - Download JAR files

2. **Direct Download** (from action summary)
   - Workflow shows artifact paths
   - Can download directly from summary

### Retention
- Default: 90 days
- Configurable in workflow YAML

### Size Expectations
```
motadata-javaagent.jar          ~100-150 MB
opentelemetry-resources-*.jar   ~12 KB
```

---

## 6. Environment Variables & Secrets

### Automatically Available
- `GITHUB_TOKEN` - Used for publishing
- `GRADLE_OPTS` - Set to `-Xmx2g` for larger heap

### Required Secrets (if publishing to Maven Central)
These would need to be added to repository secrets:
- `MAVEN_USERNAME`
- `MAVEN_PASSWORD`
- `MAVEN_GPG_KEY`
- `MAVEN_GPG_KEY_PASSWORD`

Currently not configured - workflows use GitHub Packages only.

---

## 7. Scheduled Builds

### Option A: Add Scheduled Trigger
To run builds on a schedule (e.g., nightly), modify workflow:

```yaml
on:
  schedule:
    - cron: '0 2 * * *'  # 2 AM UTC daily
  # ... other triggers
```

### Option B: External Scheduler
Use GitHub's scheduled actions or cron service.

---

## 8. Build Status Badge

Add to README.md:

```markdown
[![Build Status](https://github.com/motadata2025/opentelemetry-java-instrumentation/actions/workflows/build-javaagent.yml/badge.svg)](https://github.com/motadata2025/opentelemetry-java-instrumentation/actions/workflows/build-javaagent.yml)
```

---

## 9. Common Tasks

### Get Latest Java Agent
```bash
# 1. Go to Actions tab
# 2. Click "Build Motadata Java Agent"
# 3. Select latest successful run
# 4. Download motadata-javaagent artifact
```

### Create Release
```bash
# Option 1: GitHub Web UI
# Releases tab → "Create a new release"
# Workflow auto-publishes artifacts

# Option 2: Via Workflow Dispatch
# Actions → "Build Motadata Java Agent"
# Run workflow with release=true
```

### Monitor Builds
```bash
# Recommended:
# 1. Watch the repository (GitHub UI)
# 2. Enable notifications for Actions
# 3. Check workflow runs regularly
```

### Debug Failed Build
```bash
# 1. Click failed workflow run
# 2. Expand step logs
# 3. Look for error messages
# 4. Fix code and push new commit
```

---

## 10. Workflow Configuration Reference

### Build Triggers
```yaml
# Trigger on code changes
on:
  push:
    branches: [main, motadata-dev]
    paths:
      - 'javaagent/**'
      - 'instrumentation/resources/motadata-service-detector/**'
```

### Test Configuration
```yaml
# Java version (uses 21 LTS for compatibility)
java-version: '21'

# Gradle options
GRADLE_OPTS: '-Xmx2g'
```

### Artifact Upload
```yaml
# Upload to GitHub
- uses: actions/upload-artifact@v4
  with:
    name: motadata-javaagent
    path: javaagent/build/libs/opentelemetry-javaagent.jar
    retention-days: 90
```

---

## 11. Troubleshooting

### Build Fails: "Gradle Daemon Disappeared"
**Solution**: Increase heap memory
```yaml
GRADLE_OPTS: '-Xmx3g'  # Increase from 2g
```

### Tests Fail: "GradleWorkerMain Detected"
**Solution**: This is normal in CI. Tests clear `sun.java.command` property.

### Artifacts Not Uploading
**Solution**: Check file paths exist
```bash
# Verify file exists before upload
ls -la javaagent/build/libs/opentelemetry-javaagent.jar
```

### GitHub Packages Auth Fails
**Solution**: Ensure `GITHUB_TOKEN` permissions
```yaml
permissions:
  packages: write
  contents: read
```

---

## 12. Best Practices

### ✅ Do's
- Run tests before merging PRs (CI workflow)
- Build complete agent on main branch
- Create releases for stable versions
- Monitor workflow logs for errors
- Clean up old artifacts (90-day retention)

### ❌ Don'ts
- Force push after CI fails
- Ignore test failures
- Use debug secrets in logs
- Build on every commit (use triggers)

---

## 13. Next Steps

1. **Enable Workflows**
   ```bash
   git add .github/workflows/*.yml
   git commit -m "Add GitHub Actions CI/CD pipelines"
   git push
   ```

2. **Test Workflows**
   - Create PR to trigger CI
   - Watch workflow run
   - Verify artifacts upload

3. **Configure Notifications**
   - GitHub Settings → Notifications
   - Enable "Actions" notifications

4. **Monitor Builds**
   - Add to dashboard
   - Set up Slack/email alerts

---

## 14. Quick Reference

| Workflow | Trigger | Duration | Output |
|----------|---------|----------|--------|
| CI | PR / Push | 2-3 min | Test results |
| Build | PR / Push / Manual | 10-15 min | JAR artifacts |
| Publish | Release / Manual | 15-20 min | Packages |

---

## 15. Support & Examples

### Example: Full Release Process
```bash
# 1. Make changes
git add .
git commit -m "Feature X"
git push origin motadata-dev

# 2. Create PR
# → CI workflow runs automatically
# → Download artifact to test locally

# 3. Merge to main
# → Build workflow runs
# → Complete agent JAR produced

# 4. Create Release
# GitHub Releases tab → "New release"
# → Publish workflow runs
# → JAR available in packages
```

### Example: Manual Build
```bash
# 1. Go to GitHub Actions
# 2. Click "Build Motadata Java Agent"
# 3. "Run workflow" → select branch
# 4. Wait for build (10-15 min)
# 5. Download artifacts
```

---

For questions or issues, see the main README.md in the repository root.
