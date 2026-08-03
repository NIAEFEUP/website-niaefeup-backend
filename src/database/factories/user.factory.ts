import { faker } from '@faker-js/faker';
import { setSeederFactory } from 'typeorm-extension';
import { User } from '@/users/entities/user.entity';

export default setSeederFactory(User, () => {
  const user = new User();
  user.name = faker.person.fullName();
  user.email = faker.internet.email().toLowerCase();
  user.password = faker.internet.password({ length: 8 });
  user.isActive = true;

  return user;
});
