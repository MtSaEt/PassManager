# PassManager

Keeping things simple for the user, atleast as simple as they can get, to keep their passwords safe locally on their phone.

This is an application that I developed in roughly 4 months; new projects came along and the project got put on hold.

### Application Features

Currently the application features consists of:

* Custom Pin Length Protection
* Fingerprint Protection option for devices with fingerprint capabilities
* Import / Export your current list to .csv (non-encrypted) document
* Username Autocompletion (when adding an account, suggest an already entered username)
* Search
* Sort
* CRUD for entries
* Password Generation
* Undo on delete
* Multiple delete

### Todo / Missing features
Here is a list of the backlog during the development process:
* Have better reusability across the application (code)
* Complete/redo the introduction for first-time launches
* Double security on a specific entry => Would give a purpose to the down arrow. 
(This would be used alongside the implementation of an **ExpandableListView**)
* **Password Encryption**
* Make landscape version of every layout
* Properly obfuscate code for production
* Dark Theme
* Add `space` choice to password generation
  * Rework password generation file for flexibility
* Implement favorites

### Ideas
Here are some ideas I've had for the application, but still not sure if it would be relevant to actually do them

* Click on entry type icon to open wifi settings, web browser, or application
* ViewPager (Basic View, Advanced View) for a more advanced UX
* Password tips
* Password strength
* Fingerprint Dialog Box

## Contributing guidelines

I would love for you to help improve this project. To help keep this application
high quality;

- **Please only add/modify/delete *one feature* per pull request**. This helps keep pull
  requests and feedback focused on a specific section of the application.

## Contributing workflow

Here’s how you can contribute to this project:

1. [Fork this project][fork] to your account.
2. [Create a branch][branch] for the change you intend to make.
3. Make your changes to your fork.
4. [Send a pull request][pr] from your fork’s branch to the `master` branch.

[fork]: https://help.github.com/articles/fork-a-repo/
[branch]: https://help.github.com/articles/creating-and-deleting-branches-within-your-repository
[pr]: https://help.github.com/articles/using-pull-requests/

## License
MIT License
