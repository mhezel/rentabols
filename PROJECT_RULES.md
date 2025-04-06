# Project Rules and Guidelines

## Core Principles

- Always prefer simple, readable solutions over complex ones
- Keep the codebase clean, consistent, and well-organized
- Think before adding new patterns or technologies
- Follow Android best practices and Material Design guidelines
- Maintain backward compatibility when possible

## Code Quality Standards

### General
- Avoid code duplication by checking existing implementations first
- Use descriptive, consistent naming conventions for variables, functions, and classes
- Add meaningful comments to explain complex logic or important design decisions
- Maintain consistent formatting, indentation, and spacing throughout the codebase
- Follow Kotlin coding conventions and style guide

### Android Specific
- Use Kotlin as the primary language
- Follow MVVM architecture pattern
- Implement proper error handling and logging
- Use AndroidX libraries
- Follow Material Design guidelines
- Implement proper lifecycle management
- Use ViewBinding or DataBinding for view references
- Follow proper resource naming conventions

## Development Practices

### Version Control
- Leverage version control effectively for all code changes
- Write clear, descriptive commit messages
- Create feature branches for new development
- Keep commits focused and atomic
- Review code before merging to main branch

### Code Organization
- Avoid single-use scripts in source files; consider more appropriate locations
- When fixing bugs, exhaust all options using existing patterns before introducing new ones
- Keep resource files organized and properly named
- Use appropriate package structure
- Follow the single responsibility principle

### Security
- ⚠️ Never modify .env files without explicit prior confirmation
- Never commit sensitive information or API keys
- Use proper encryption for sensitive data
- Follow Android security best practices
- Implement proper permission handling

### Testing
- Write unit tests for critical business logic
- Implement UI tests for important user flows
- Maintain test coverage for core functionality
- Use proper testing frameworks and tools
- Document test cases and requirements

### Documentation
- Keep documentation up to date
- Document complex algorithms and business logic
- Maintain clear API documentation
- Update README with important changes
- Document architectural decisions

## Code Review Guidelines

- Review code for adherence to these guidelines
- Check for potential security issues
- Verify proper error handling
- Ensure proper resource management
- Validate performance considerations
- Check for proper documentation
- Verify test coverage

## Performance Guidelines

- Optimize app startup time
- Minimize memory usage
- Implement proper caching strategies
- Optimize network calls
- Use appropriate data structures
- Implement proper background processing
- Follow Android performance best practices

## Accessibility

- Follow Android accessibility guidelines
- Implement proper content descriptions
- Ensure proper contrast ratios
- Support screen readers
- Test with accessibility tools
- Consider different user needs

Remember: These guidelines are meant to help maintain code quality and consistency. If you have any questions or suggestions for improvement, please discuss with the team. 