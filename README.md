## Smaug
An interactive crafting plugin for Minecraft servers to create custom items. The plugin allows you to define custom items either in it's own plugin configuration or register your items externally using the API. 

### Features
- Define recipes/conversions to create custom items
### Usage
Defining an item in-configuration can be done like so:

```
test:
    material: DIAMOND
    display-name: <#000000>This is a test item! 
    item-model: minecraft:stone
```
Note: all text-based fields such as display-name, or lore can use [MiniMessage](https://docs.advntr.dev/minimessage/format) format.

You can inherit properties additively by using the key 'inherits'

```
test2:
    display-name: <#0000ff>This is the second test item!
    inherits: test
```
### Links
- Support for the plugin and it's API are available on [discord](https://discord.gg/yrnqw5S2). Any questions that cannot be answered with the content on this page can be asked there. 
- Reporting a bug? [please open a ticket on github](https://www.github.com/mintychochip/smaug).

