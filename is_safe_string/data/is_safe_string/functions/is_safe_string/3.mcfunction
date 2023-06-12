data modify storage is_safe_string: one set from storage is_safe_string_data: one
execute store result score #1 is_safe_string run data modify storage is_safe_string: one[] set from storage is_safe_string: scrutinee
data remove storage is_safe_string: one
execute if score #1 is_safe_string matches ..126 run return 0
scoreboard players add #0 is_safe_string 1
data modify storage is_safe_string: two set from storage is_safe_string_data: two
execute store result score #1 is_safe_string run data modify storage is_safe_string: two[] set from storage is_safe_string: scrutinee
data remove storage is_safe_string: two
execute if score #1 is_safe_string matches ..1920 run return 0
scoreboard players add #0 is_safe_string 1
