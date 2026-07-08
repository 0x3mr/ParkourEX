A simple parkour plugin in beta stage.

TODO:

- [x] Place an indicating hologram at each checkpoint
- [x] Add a Commands list command
- [x] Add a reset option during parkour
- [x] Add a leave option during parkour
- [ ] Edit display content of parkour messages & add real persistent analytics
    - Best time
    - Personal best between each consecutive checkpoints
    - Parkours completed
    - Incomplete parkours
- [ ] Add permissions hierarchy to the plugin
- [ ] Add a command to list current available parkour games (perhaps a GUI)
- [x] Add option to let parkour continuable upon checkpoint skips
- [x] Improve the existing hologram tags design to become consistent
- [ ] Bug fix: teleport back to checkpoint when not in a parkour session
  - unify/centralize teleport back to checkpoint logic & reuse
- [ ] Remove fall damage during parkour session
  - make sure it does not break external functionalities on parkour exit
- [ ] Remove player collision while in parkour
- [ ] Add config-toggleable feature to auto teleport back to checkpoint on reaching certain Y-level (preset in config)
- [ ] Let parkour items configurable
  - let items management be centralized in one place
- [ ] Remove any effects the player has during a parkour
- [ ] Add customizable config-set player & console to-run commands on parkour session exit