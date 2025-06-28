# Branch Protection and Workflow Rules

## Branch Strategy

### Main Branches
- `main` - Production-ready code

### Feature Branches
Format: `feature/BR-{serial_number}-{description}`

Examples:
- `feature/BR-001-add-user-profile`
- `feature/BR-002-implement-email-verification`
- `feature/BR-003-add-password-reset`

## Branch Protection Rules

### Main Branch (`main`)
- ✅ Require pull request reviews before merging
- ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date before merging
- ✅ Require linear history
- ✅ Restrict pushes that create files larger than 100 MB
- ✅ Require conversation resolution before merging

## Workflow Rules

### Feature Development
1. Create feature branch from `main` using format: `feature/BR-{serial_number}-{description}`
2. Make changes and commit with descriptive messages
3. Push branch and create pull request to `main`
4. Ensure all CI checks pass
5. Get code review approval
6. Merge to `main`

### Release Process
1. All features are merged directly to `main` through pull requests
2. Each merge triggers the full CI/CD pipeline
3. Production deployment happens automatically on successful merge to `main`

### Hotfix Process
1. Create hotfix branch from `main` using format: `hotfix/BR-{serial_number}-{description}`
2. Fix the issue
3. Create pull request to `main`
4. Get approval and merge

## Commit Message Format

Use conventional commits format:
```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Examples:
- `feat(user): add user profile management`
- `fix(auth): resolve authentication token issue`
- `docs(api): update API documentation`

## Code Quality Standards

### Required Checks
- ✅ All tests must pass
- ✅ Code coverage > 80%
- ✅ Code formatting (Spotless)
- ✅ Dependency vulnerability check

### Review Guidelines
- Code must be reviewed by at least one maintainer
- All CI checks must pass
- No merge conflicts
- Follow coding standards and conventions
- Include appropriate tests for new features

## Environment Strategy

- **Development**: Local development with Docker
- **Production**: Automated deployment from `main` branch

## Security

- Dependencies are checked for known vulnerabilities
- Secrets are managed through GitHub Secrets
- Access to production environment is restricted
- Regular code reviews and testing 