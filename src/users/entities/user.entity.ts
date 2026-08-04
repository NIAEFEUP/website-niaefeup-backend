import { ApiProperty } from "@nestjs/swagger";
import { Column, Entity, PrimaryGeneratedColumn } from "typeorm";

@Entity()
export class User {
  @ApiProperty({ description: "User ID", example: 1 })
  @PrimaryGeneratedColumn()
  id: number;

  @ApiProperty({ description: "User's full name", example: "John Doe" })
  @Column()
  name: string;

  @ApiProperty({
    description: "User's email address",
    example: "john@example.com",
  })
  @Column({ unique: true })
  email: string;

  @Column()
  password: string;

  @ApiProperty({ description: "Whether the user is active", example: true })
  @Column({ default: true })
  isActive: boolean;
}
