execute store result score #0 is_safe_string run data get storage is_safe_string: input
execute if score #0 is_safe_string matches ..21845 run data modify storage is_safe_string: output set value true
execute if score #0 is_safe_string matches 65536.. run data modify storage is_safe_string: output set value false
execute if score #0 is_safe_string matches 21846..65535 run function is_safe_string:is_safe_string/1
data remove storage is_safe_string: input
