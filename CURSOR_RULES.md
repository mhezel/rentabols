# Cursor Development Rules and Guidelines

## Core Principles

- **Simplicity First**: Always prefer simple, readable solutions over complex ones
- **Code Quality**: Keep the codebase clean, consistent, and well-organized
- **Thoughtful Development**: Think before adding new patterns or technologies
- **Design Standards**: Follow Android best practices and Material Design guidelines
- **Compatibility**: Maintain backward compatibility when possible

## Code Quality Standards

### General
- **DRY Principle**: Avoid code duplication by checking existing implementations first
- **Naming**: Use descriptive, consistent naming conventions for variables, functions, and classes
- **Documentation**: Add meaningful comments to explain complex logic or important design decisions
- **Formatting**: Maintain consistent formatting, indentation, and spacing throughout the codebase
- **Kotlin Standards**: Follow Kotlin coding conventions and style guide

### Android Specific
- **Language**: Use Kotlin as the primary language
- **Architecture**: Follow MVVM architecture pattern
- **Error Handling**: Implement proper error handling and logging
- **Libraries**: Use AndroidX libraries
- **Design**: Follow Material Design guidelines
- **Lifecycle**: Implement proper lifecycle management
- **View Binding**: Use ViewBinding or DataBinding for view references
- **Resources**: Follow proper resource naming conventions

## Development Practices

### Version Control
- **Git Usage**: Leverage version control effectively for all code changes
- **Commits**: Write clear, descriptive commit messages
- **Branches**: Create feature branches for new development
- **Atomic Commits**: Keep commits focused and atomic
- **Code Review**: Review code before merging to main branch

### Code Organization
- **Script Management**: Avoid single-use scripts in source files; consider more appropriate locations
- **Bug Fixing**: When fixing bugs, exhaust all options using existing patterns before introducing new ones
- **Resource Management**: Keep resource files organized and properly named
- **Package Structure**: Use appropriate package structure
- **Single Responsibility**: Follow the single responsibility principle

### Security
- **Environment Files**: ⚠️ Never modify .env files without explicit prior confirmation
- **Sensitive Data**: Never commit sensitive information or API keys
- **Encryption**: Use proper encryption for sensitive data
- **Android Security**: Follow Android security best practices
- **Permissions**: Implement proper permission handling

### Testing
- **Unit Tests**: Write unit tests for critical business logic
- **UI Tests**: Implement UI tests for important user flows
- **Coverage**: Maintain test coverage for core functionality
- **Frameworks**: Use proper testing frameworks and tools
- **Documentation**: Document test cases and requirements

### Documentation
- **Maintenance**: Keep documentation up to date
- **Complex Logic**: Document complex algorithms and business logic
- **API Docs**: Maintain clear API documentation
- **README**: Update README with important changes
- **Architecture**: Document architectural decisions

## Code Review Guidelines

- **Guideline Adherence**: Review code for adherence to these guidelines
- **Security Check**: Check for potential security issues
- **Error Handling**: Verify proper error handling
- **Resource Management**: Ensure proper resource management
- **Performance**: Validate performance considerations
- **Documentation**: Check for proper documentation
- **Testing**: Verify test coverage

## Performance Guidelines

- **Startup Time**: Optimize app startup time
- **Memory Usage**: Minimize memory usage
- **Caching**: Implement proper caching strategies
- **Networking**: Optimize network calls
- **Data Structures**: Use appropriate data structures
- **Background Processing**: Implement proper background processing
- **Android Performance**: Follow Android performance best practices

## Accessibility

- **Guidelines**: Follow Android accessibility guidelines
- **Content Descriptions**: Implement proper content descriptions
- **Contrast**: Ensure proper contrast ratios
- **Screen Readers**: Support screen readers
- **Testing**: Test with accessibility tools
- **User Needs**: Consider different user needs

> **Note**: These guidelines are meant to help maintain code quality and consistency. If you have any questions or suggestions for improvement, please discuss with the team. 