package com.shf.reggie.dto;

import com.shf.reggie.entity.Setmeal;
import com.shf.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
