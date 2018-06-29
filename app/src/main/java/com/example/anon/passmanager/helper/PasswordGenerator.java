package com.example.anon.passmanager.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Anon on 2017-03-15.
 */

public class PasswordGenerator {
    
    public String generatePassword(boolean lowercase, boolean uppercase, boolean numbers, boolean symbols, int length) {
        String[] cU = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split(""),
                cL = "abcdefghijklmnopqrstuvwxyz".split(""),
                nums = "0123456789".split(""),
                specialC = "!?$?%^&*()_-+={[}]:;@~#|<>.?/".split("");

        String pwd = "";
        String[] merged = {};
        ArrayList<String> mustInclude = new ArrayList<>();

        if (!lowercase && !uppercase && numbers && symbols) {
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt(specialC.length)]);
            merged = join(nums, specialC);
        } else if (!lowercase && !uppercase && numbers && !symbols) {
            merged = nums;
        } else if (!lowercase && !uppercase && !numbers && symbols) {
            merged = specialC;
        } else if (!lowercase && uppercase && !numbers && !symbols) {
            merged = cU;
        } else if (!lowercase && uppercase && numbers && symbols) {
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt((specialC.length - 1)) + 1]);
            merged = join(cU, nums, specialC);
        } else if (!lowercase && uppercase && !numbers && symbols) {
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt((specialC.length - 1)) + 1]);
            merged = join(cU, specialC);
        } else if (!lowercase && uppercase && numbers && !symbols) {
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            merged = join(cU, nums);
        } else if (lowercase && uppercase && numbers && symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt((specialC.length - 1)) + 1]);
            merged = join(cL, cU, nums, specialC);
        } else if (lowercase && uppercase && numbers && !symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            merged = join(cL, cU, nums);
        } else if (lowercase && uppercase && !numbers && symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt((specialC.length - 1)) + 1]);
            merged = join(cL, cU, specialC);
        } else if (lowercase && uppercase && !numbers && !symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(cU[new Random().nextInt((cU.length - 1)) + 1]);
            merged = join(cL, cU);
        } else if (lowercase && !uppercase && numbers && symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt((specialC.length - 1)) + 1]);
            merged = join(cL, nums, specialC);
        } else if (lowercase && !uppercase && !numbers && symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(specialC[new Random().nextInt((specialC.length - 1)) + 1]);
            merged = join(cL, specialC);
        } else if (lowercase && !uppercase && !numbers && !symbols) {
            merged = cL;
        } else if (lowercase && !uppercase && numbers && !symbols) {
            mustInclude.add(cL[new Random().nextInt((cL.length - 1)) + 1]);
            mustInclude.add(nums[new Random().nextInt((nums.length - 1)) + 1]);
            merged = join(cL, nums);
        }

        while (pwd.length() < (length - mustInclude.size())) {
            int rnd = new Random().nextInt(merged.length);
            pwd += merged[rnd];
        }

        while (!mustInclude.isEmpty()) {
            if (pwd.length() + mustInclude.size() > mustInclude.size()) {
                pwd = new StringBuilder(pwd).insert(pwd.length() - new Random().nextInt(pwd.length()),
                        mustInclude.get(0)).toString();
                mustInclude.remove(0);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : mustInclude) {
                    sb.append(s);
                }
                pwd = sb.toString();
                break;
            }
        }
        return pwd;
    }

    private static String[] join(String[]...arrays) {
        final List<String> output = new ArrayList<>();
        for(String[] array : arrays) {
            output.addAll(Arrays.asList(array));
        }
        return output.toArray(new String[output.size()]);
    }
}
