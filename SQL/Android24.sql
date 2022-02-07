-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema android24
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema android24
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `android24` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `android24` ;

-- -----------------------------------------------------
-- Table `android24`.`races`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`races` ;

CREATE TABLE IF NOT EXISTS `android24`.`races` (
  `RaceName` VARCHAR(100) NOT NULL,
  `BaseHealth` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `BaseKi` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `BaseStrikeAttack` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `BaseKiAttack` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `BaseDefence` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `BaseSpeed` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`RaceName`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'All the races in the game';


-- -----------------------------------------------------
-- Table `android24`.`shop`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`shop` ;

CREATE TABLE IF NOT EXISTS `android24`.`shop` (
  `Name` VARCHAR(100) NOT NULL,
  `LinkedTo` VARCHAR(100) NULL DEFAULT NULL,
  `Depended` VARCHAR(100) NULL DEFAULT NULL,
  `ForcedRace` VARCHAR(100) NULL DEFAULT NULL,
  `Cost` BIGINT UNSIGNED NOT NULL DEFAULT '0',
  `MinimalLevel` INT UNSIGNED NOT NULL DEFAULT '0',
  `Description` VARCHAR(2083) NULL DEFAULT 'There is no descriptoin avalible',
  `Gif` VARCHAR(2083) NULL,
  PRIMARY KEY (`Name`),
  INDEX `shop_LinkedTo_shop_Name` (`LinkedTo` ASC) INVISIBLE,
  INDEX `shop_Depended_shop_Name` (`Depended` ASC) VISIBLE,
  INDEX `shop_ForcedRace_races_RaceName` (`ForcedRace` ASC) VISIBLE,
  UNIQUE INDEX `Depended_UNIQUE` (`Depended` ASC) VISIBLE,
  CONSTRAINT `shop_Depended_shop_Name`
    FOREIGN KEY (`Depended`)
    REFERENCES `android24`.`shop` (`Name`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `shop_ForcedRace_races_RaceName`
    FOREIGN KEY (`ForcedRace`)
    REFERENCES `android24`.`races` (`RaceName`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `shop_LinkedTo_shop_Name`
    FOREIGN KEY (`LinkedTo`)
    REFERENCES `android24`.`shop` (`Name`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'The table that will describe all the items';


-- -----------------------------------------------------
-- Table `android24`.`attacks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`attacks` ;

CREATE TABLE IF NOT EXISTS `android24`.`attacks` (
  `AttackName` VARCHAR(100) NOT NULL,
  `AttackShortcut` VARCHAR(16) NOT NULL,
  `AttackPowerUp` INT UNSIGNED NOT NULL DEFAULT '1',
  `DefencePowerUp` INT UNSIGNED NOT NULL DEFAULT '1',
  `SpeedPowerUp` INT UNSIGNED NOT NULL DEFAULT '1',
  `KiConsumption` INT UNSIGNED NOT NULL DEFAULT '1',
  `Counter` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `AttackType` ENUM('Strike', 'Ki') NOT NULL,
  PRIMARY KEY (`AttackName`, `AttackShortcut`),
  UNIQUE INDEX `attackscol_UNIQUE` (`AttackShortcut` ASC) INVISIBLE,
  UNIQUE INDEX `AttackName_UNIQUE` (`AttackName` ASC) VISIBLE,
  CONSTRAINT `attacks_AttackName_shop_Name`
    FOREIGN KEY (`AttackName`)
    REFERENCES `android24`.`shop` (`Name`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'The table that will describe all the attacks';


-- -----------------------------------------------------
-- Table `android24`.`scouters`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`scouters` ;

CREATE TABLE IF NOT EXISTS `android24`.`scouters` (
  `ScouterName` VARCHAR(100) NOT NULL,
  `PLLimit` BIGINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'The power level limit of the scouter',
  PRIMARY KEY (`ScouterName`),
  CONSTRAINT `scouters_ScouterName_shop_Name`
    FOREIGN KEY (`ScouterName`)
    REFERENCES `android24`.`shop` (`Name`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'The tables that describes the scouters';


-- -----------------------------------------------------
-- Table `android24`.`users_data`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`users_data` ;

CREATE TABLE IF NOT EXISTS `android24`.`users_data` (
  `UserID` BIGINT UNSIGNED NOT NULL,
  `UserName` VARCHAR(100) NOT NULL,
  `Race` VARCHAR(100) NULL,
  `XP` BIGINT UNSIGNED NOT NULL DEFAULT '0',
  `Zeni` BIGINT NOT NULL DEFAULT '0',
  `PowerPoints` INT NOT NULL DEFAULT '0',
  `Health` INT UNSIGNED NOT NULL DEFAULT '0',
  `Ki` INT UNSIGNED NOT NULL DEFAULT '0',
  `StrikeAttack` INT UNSIGNED NOT NULL DEFAULT '0',
  `KiAttack` INT UNSIGNED NOT NULL DEFAULT '0',
  `Defence` INT UNSIGNED NOT NULL DEFAULT '0',
  `Speed` INT UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`UserID`),
  INDEX `race` (`Race` ASC) INVISIBLE,
  CONSTRAINT `users_data_TO_races`
    FOREIGN KEY (`Race`)
    REFERENCES `android24`.`races` (`RaceName`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'Where we store most of the data of the users';


-- -----------------------------------------------------
-- Table `android24`.`weapons`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`weapons` ;

CREATE TABLE IF NOT EXISTS `android24`.`weapons` (
  `WeaponName` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`WeaponName`),
  CONSTRAINT `weapons_WeaponName_shop_Name`
    FOREIGN KEY (`WeaponName`)
    REFERENCES `android24`.`shop` (`Name`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'The treble that describes the weapons';


-- -----------------------------------------------------
-- Table `android24`.`inventory`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`inventory` ;

CREATE TABLE IF NOT EXISTS `android24`.`inventory` (
  `UserID` BIGINT UNSIGNED NOT NULL,
  `Zenso` INT UNSIGNED NOT NULL DEFAULT '0',
  `Scouter` VARCHAR(100) NULL DEFAULT NULL,
  `Weapon` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`UserID`),
  INDEX `inventory_Scouter_scouters_ScouterName` (`Scouter` ASC) VISIBLE,
  INDEX `inventory_Weapon_weapons_WeaponName` (`Weapon` ASC) VISIBLE,
  CONSTRAINT `inventory_Scouter_scouters_ScouterName`
    FOREIGN KEY (`Scouter`)
    REFERENCES `android24`.`scouters` (`ScouterName`)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `inventory_UserID_users_data_UserID`
    FOREIGN KEY (`UserID`)
    REFERENCES `android24`.`users_data` (`UserID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `inventory_Weapon_weapons_WeaponName`
    FOREIGN KEY (`Weapon`)
    REFERENCES `android24`.`weapons` (`WeaponName`)
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'Where we save the inventory of the users';


-- -----------------------------------------------------
-- Table `android24`.`transformations`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`transformations` ;

CREATE TABLE IF NOT EXISTS `android24`.`transformations` (
  `TransformationName` VARCHAR(100) NOT NULL,
  `TransformationShortcut` VARCHAR(16) NOT NULL,
  `AttackPowerUp` INT UNSIGNED NOT NULL DEFAULT '1',
  `DefencePowerUp` INT UNSIGNED NOT NULL DEFAULT '1',
  `SpeedPowerUp` INT UNSIGNED NOT NULL DEFAULT '1',
  `KiConsumption` DECIMAL(10,0) UNSIGNED NOT NULL DEFAULT '1',
  `SoloTransformation` TINYINT UNSIGNED NOT NULL DEFAULT 1,
  PRIMARY KEY (`TransformationName`, `TransformationShortcut`),
  UNIQUE INDEX `TransformationName_UNIQUE` (`TransformationName` ASC) VISIBLE,
  UNIQUE INDEX `TransformationShortcut_UNIQUE` (`TransformationShortcut` ASC) VISIBLE,
  CONSTRAINT `transformations_TransformationName_shop_Name`
    FOREIGN KEY (`TransformationName`)
    REFERENCES `android24`.`shop` (`Name`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `android24`.`users_attacks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`users_attacks` ;

CREATE TABLE IF NOT EXISTS `android24`.`users_attacks` (
  `UserID` BIGINT UNSIGNED NOT NULL,
  `AttackShortcut` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`UserID`, `AttackShortcut`),
  INDEX `users_attacks_AttackName_attacks_AttackName` (`AttackShortcut` ASC) VISIBLE,
  CONSTRAINT `users_attacks_AttackName_attacks_AttackName`
    FOREIGN KEY (`AttackShortcut`)
    REFERENCES `android24`.`attacks` (`AttackShortcut`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `users_attacks_UserID_users_data_UserID`
    FOREIGN KEY (`UserID`)
    REFERENCES `android24`.`users_data` (`UserID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'The table that is connecting the users to their attacks';


-- -----------------------------------------------------
-- Table `android24`.`users_transformations`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `android24`.`users_transformations` ;

CREATE TABLE IF NOT EXISTS `android24`.`users_transformations` (
  `UserID` BIGINT UNSIGNED NOT NULL,
  `TransformationShortcut` VARCHAR(16) NOT NULL,
  PRIMARY KEY (`UserID`, `TransformationShortcut`),
  INDEX `users_transformations-transformations: TransformationName` (`TransformationShortcut` ASC) VISIBLE,
  CONSTRAINT `users_transformations-transformations: TransformationName`
    FOREIGN KEY (`TransformationShortcut`)
    REFERENCES `android24`.`transformations` (`TransformationShortcut`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `users_transformations_UserID_users_data_UserID`
    FOREIGN KEY (`UserID`)
    REFERENCES `android24`.`users_data` (`UserID`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = 'The table that is connecting between the users and their available transformations';


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
