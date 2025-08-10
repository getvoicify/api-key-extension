# Automated Semantic Versioning

This project uses automated semantic versioning based on [Conventional Commits](https://www.conventionalcommits.org/) specification.

## How It Works

### 1. Commit Message Format
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 2. Version Bumping Rules

| Commit Type | Version Bump | Example |
|-------------|--------------|---------|
| `feat:` or `feature:` | **Minor** (1.0.0 → 1.1.0) | New feature added |
| `fix:`, `bugfix:`, `hotfix:` | **Patch** (1.0.0 → 1.0.1) | Bug fixes |
| `BREAKING CHANGE:` or `!:` | **Major** (1.0.0 → 2.0.0) | Breaking changes |
| `chore:`, `docs:`, `style:`, `test:` | **No bump** | Maintenance tasks |

### 3. Automatic Release Process

When you push to the `main` branch:

1. **Analysis**: The workflow analyzes commit messages since the last release
2. **Version Calculation**: Determines the next version based on commit types
3. **Conditional Release**: Only creates releases if version-bumping commits are found
4. **Artifacts**: Builds JAR, creates checksums, and uploads to GitHub Packages
5. **GitHub Release**: Automatically creates a GitHub release with changelog

## Examples

### Feature Addition (Minor Bump)
```bash
git commit -m "feat: add API key validation endpoint"
# Results in: 1.0.0 → 1.1.0
```

### Bug Fix (Patch Bump)
```bash
git commit -m "fix: resolve authentication issue in API key creation"
# Results in: 1.0.0 → 1.0.1
```

### Breaking Change (Major Bump)
```bash
git commit -m "feat!: redesign API key storage format

BREAKING CHANGE: API key format has changed and is not backward compatible"
# Results in: 1.0.0 → 2.0.0
```

### No Version Bump
```bash
git commit -m "chore: update documentation"
git commit -m "docs: add API usage examples" 
git commit -m "style: apply code formatting"
# Results in: No release created
```

### Multiple Commits
If you have multiple commits, the highest version bump wins:
```bash
git commit -m "chore: update dependencies"  # No bump
git commit -m "fix: resolve memory leak"     # Patch
git commit -m "feat: add new endpoint"       # Minor
# Results in: 1.0.0 → 1.1.0 (minor wins)
```

## Advanced Examples

### Scoped Commits
```bash
git commit -m "feat(auth): add OAuth2 support"
git commit -m "fix(api): handle null API keys gracefully"
git commit -m "docs(readme): update installation instructions"
```

### Multi-line Commits
```bash
git commit -m "feat: implement API key rotation

- Add endpoint for rotating existing API keys
- Maintain backward compatibility during transition period
- Add rate limiting for rotation requests"
```

### Breaking Change in Body
```bash
git commit -m "refactor: redesign user authentication

This commit changes the authentication flow significantly.

BREAKING CHANGE: Authentication endpoints now require different headers"
```

## Workflow Behavior

### On Main Branch Push
- ✅ **Analyzes commits** since last release
- ✅ **Calculates semantic version** based on conventional commits
- ✅ **Runs full CI/CD pipeline** (lint, test, build, security scan)
- ✅ **Publishes to GitHub Packages** if version bump detected
- ✅ **Creates GitHub Release** with changelog if version bump detected
- ✅ **Skips release creation** if no version-bumping commits found

### On Pull Requests
- ✅ **Runs CI/CD pipeline** (lint, test, build, security scan)
- ❌ **No versioning or publishing** (safe for development)

### On Develop Branch Push
- ✅ **Runs CI/CD pipeline** (lint, test, build, security scan)
- ❌ **No versioning or publishing** (development branch)

## Best Practices

### 1. Use Conventional Commits
Always follow the conventional commit format for automatic versioning to work.

### 2. Squash and Merge
When merging PRs, use "Squash and Merge" with a proper conventional commit message.

### 3. Group Related Changes
```bash
# Good: Single commit for related changes
git commit -m "feat: add comprehensive API key management

- Add creation endpoint
- Add validation endpoint  
- Add rotation endpoint"

# Less ideal: Multiple commits for same feature
git commit -m "feat: add creation endpoint"
git commit -m "feat: add validation endpoint"  
git commit -m "feat: add rotation endpoint"
```

### 4. Be Explicit with Breaking Changes
```bash
# Clear breaking change
git commit -m "feat!: change API response format

BREAKING CHANGE: All API responses now return data in 'result' field instead of root level"
```

## Troubleshooting

### No Release Created
If you expected a release but none was created:
1. Check if your commit messages follow conventional commit format
2. Verify you're pushing to the `main` branch
3. Look for version-bumping commit types (`feat:`, `fix:`, `BREAKING CHANGE:`)

### Wrong Version Bump
If the version bump is incorrect:
1. Check your commit message format
2. Verify the commit type matches the intended change
3. For breaking changes, ensure `!` suffix or `BREAKING CHANGE:` footer

### Release Failed
If the release process fails:
1. Check GitHub Actions logs for specific errors
2. Verify `GITHUB_TOKEN` has appropriate permissions
3. Ensure no duplicate tags exist

## Manual Override

If you need to create a release manually:
```bash
# Create and push a tag
git tag v1.2.3
git push origin v1.2.3

# The workflow will detect the tag and create a release
```

## Version History

You can view all releases and their changelogs in the [GitHub Releases](https://github.com/voicify/keycloak-api-key-extension/releases) page.