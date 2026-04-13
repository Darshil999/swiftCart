# swiftCart

A shopping cart project repository.

---

## How to Add Your Entire Project Folder to This Repository

Follow these steps to push your local project folder into this GitHub repository.

### Prerequisites

- [Git](https://git-scm.com/downloads) installed on your machine
- A [GitHub account](https://github.com) with access to this repository

---

### Steps

#### 1. Open a terminal in your project folder

Navigate to the root of your project directory:

```bash
cd /path/to/your/project
```

#### 2. Initialize a Git repository (if not already done)

```bash
git init
```

> **Skip this step** if your project folder already contains a `.git` directory.

#### 3. Add a `.gitignore` file (recommended)

Create a `.gitignore` file to exclude files you don't want to commit (e.g., `node_modules`, build artifacts, environment files):

```bash
# Example: create a .gitignore for a Node.js project
echo "node_modules/" >> .gitignore
echo ".env" >> .gitignore
echo "dist/" >> .gitignore
```

> See the `.gitignore` file in this repository for a ready-to-use template.

#### 4. Stage all your files

```bash
git add .
```

#### 5. Commit your files

```bash
git commit -m "Initial commit: add project files"
```

#### 6. Link your local repo to this GitHub repository

```bash
git remote add origin https://github.com/Darshil999/swiftCart.git
```

> **Skip this step** if a remote named `origin` already exists. You can check with:
> ```bash
> git remote -v
> ```

#### 7. Push your project to GitHub

If this is your first push:

```bash
git push -u origin main
```

> If your default branch is named `master` instead of `main`, use:
> ```bash
> git push -u origin master
> ```

---

### Pushing future changes

After the initial setup, use these commands whenever you make changes:

```bash
git add .
git commit -m "Describe your changes here"
git push
```

---

### Troubleshooting

| Problem | Solution |
|---|---|
| `remote origin already exists` | Run `git remote set-url origin https://github.com/Darshil999/swiftCart.git` |
| `failed to push — non-fast-forward` | Run `git pull origin main --rebase` then push again |
| Files not tracked | Make sure they aren't listed in `.gitignore` |
| Large files rejected | Use [Git LFS](https://git-lfs.github.com/) for files over 100 MB |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push the branch: `git push origin feature/your-feature`
5. Open a Pull Request

## License

This project is open source. See [LICENSE](LICENSE) for details.
