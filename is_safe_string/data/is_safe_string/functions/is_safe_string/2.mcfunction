data modify storage is_safe_string: scrutinee set string storage is_safe_string: input 0 1
data modify storage is_safe_string: input set string storage is_safe_string: input 1
function is_safe_string:is_safe_string/3
execute if score #0 is_safe_string matches ..65535 run function is_safe_string:is_safe_string/2
