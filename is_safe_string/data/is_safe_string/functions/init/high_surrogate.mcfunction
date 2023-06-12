data modify storage is_safe_string: three append string storage is_safe_string: high_surrogate[-1] 0 1
data remove storage is_safe_string: high_surrogate[-1]
execute if data storage is_safe_string: high_surrogate[0] run function is_safe_string:init/high_surrogate
