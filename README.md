# Fair Share

[![Circle CI](https://circleci.com/gh/artempyanykh/fair-share/tree/master.svg?style=svg)](https://circleci.com/gh/artempyanykh/fair-share/tree/master)

Travel together with friends? Manage you travel expenses with ease!

## Contributing guidelines for collaborators

If you want to add a new feature or fix a bug, you will need to follow the following process

1. Pick an open issue from the
   [open list](https://github.com/ArtemPyanykh/fair-share/issues) or create a
   new one.
2. Branch off `master`. Please use lower case letters separated by a dash, e.g.
   `my-branch`, when naming the branch.
3. Do your work in the branch. **Do not commit directly to master**.
4. Open a PR, put `Connects <issue number>` or `Fixes <issue number>` in the PR
   description.
5. Poke other collaborators to review your PR.
6. If another collaborator is happy with your PR being merged, they will put a
   name-label on it. See
   [example](https://github.com/ArtemPyanykh/fair-share/pull/11).
7. After getting **at least one** approval, you can merge your branch into
   `master`. It is _strongly_ advised to rebase on top of the latest `master`
   before merging your `branch`. Use `git merge --ff-only --no-commit` in
   console when in doubt instead or `Merge` button in UI. The main reason to
   always rebase is this makes the history of commits _so_ much cleaner!
8. Rinse and repeat.

