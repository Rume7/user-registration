#!/bin/bash

# Version Bump Script for User Registration Application
# This script automatically increments the version number in pom.xml

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --patch, -p    Increment patch version (default)"
    echo "  --minor, -m    Increment minor version"
    echo "  --major, -M    Increment major version"
    echo "  --version, -v  Show current version"
    echo "  --help, -h     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0              # Bump patch version (1.0.0 -> 1.0.1)"
    echo "  $0 --minor      # Bump minor version (1.0.0 -> 1.1.0)"
    echo "  $0 --major      # Bump major version (1.0.0 -> 2.0.0)"
}

# Function to get current version
get_current_version() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout
}

# Function to parse version components
parse_version() {
    local version=$1
    IFS='.' read -ra VERSION_PARTS <<< "$version"
    MAJOR="${VERSION_PARTS[0]}"
    MINOR="${VERSION_PARTS[1]}"
    PATCH="${VERSION_PARTS[2]}"
}

# Function to calculate new version
calculate_new_version() {
    local increment_type=$1
    local current_version=$2
    
    parse_version "$current_version"
    
    case $increment_type in
        "major")
            NEW_MAJOR=$((MAJOR + 1))
            NEW_VERSION="$NEW_MAJOR.0.0"
            ;;
        "minor")
            NEW_MINOR=$((MINOR + 1))
            NEW_VERSION="$MAJOR.$NEW_MINOR.0"
            ;;
        "patch")
            NEW_PATCH=$((PATCH + 1))
            NEW_VERSION="$MAJOR.$MINOR.$NEW_PATCH"
            ;;
        *)
            print_error "Invalid increment type: $increment_type"
            exit 1
            ;;
    esac
}

# Function to update pom.xml
update_pom_version() {
    local new_version=$1
    print_info "Updating pom.xml to version $new_version"
    mvn versions:set -DnewVersion="$new_version" -DgenerateBackupPoms=false
    print_success "pom.xml updated successfully"
}

# Function to commit and push changes
commit_and_push() {
    local new_version=$1
    local commit_message="Bump version to $new_version"
    
    print_info "Committing version bump"
    git add pom.xml
    git commit -m "$commit_message"
    
    print_info "Pushing changes"
    git push
    
    print_success "Version bump committed and pushed"
}

# Function to create git tag
create_tag() {
    local version=$1
    local tag_name="v$version"
    
    print_info "Creating git tag: $tag_name"
    git tag "$tag_name"
    git push origin "$tag_name"
    print_success "Git tag created and pushed"
}

# Main script logic
main() {
    local increment_type="patch"
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --patch|-p)
                increment_type="patch"
                shift
                ;;
            --minor|-m)
                increment_type="minor"
                shift
                ;;
            --major|-M)
                increment_type="major"
                shift
                ;;
            --version|-v)
                local current_version=$(get_current_version)
                echo "Current version: $current_version"
                exit 0
                ;;
            --help|-h)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Get current version
    local current_version=$(get_current_version)
    print_info "Current version: $current_version"
    
    # Calculate new version
    calculate_new_version "$increment_type" "$current_version"
    print_info "New version: $NEW_VERSION"
    
    # Update pom.xml
    update_pom_version "$NEW_VERSION"
    
    # Commit and push changes
    commit_and_push "$NEW_VERSION"
    
    # Create git tag
    create_tag "$NEW_VERSION"
    
    print_success "Version bump completed successfully!"
    print_info "Version changed from $current_version to $NEW_VERSION"
}

# Run main function with all arguments
main "$@" 