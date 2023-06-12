loot replace entity @s container.0 loot is_safe_string:init
data modify storage is_safe_string_data: one[9] set from entity @s Item.tag.u+000a
data modify storage is_safe_string_data: one[12] set from entity @s Item.tag.u+000d
kill
