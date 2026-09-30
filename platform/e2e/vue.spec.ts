import { expect, test } from '@playwright/test'
import path from 'node:path'

test('offline demo completes issuance, authorization and provenance without login', async ({ page }) => {
  await page.goto('/evidence')

  await expect(page.getByText('现场离线演示模式')).toBeVisible()
  await expect(page).not.toHaveURL(/login|register/)

  const demoImage = path.resolve('../photo/ce099df5b991e285684b5b5021a178d4.jpg')
  await page.locator('input[type="file"]').first().setInputFiles(demoImage)
  await expect(page.getByText('人工确认并应用到表单')).toBeVisible()
  await page.getByText('人工确认并应用到表单').click()

  await page.getByPlaceholder('请输入数字资产名称').fill('馆藏彩绘瓷茶具数字影像')
  await page.getByPlaceholder('请输入创作者姓名或机构').fill('星图共链数字化采集团队')
  await page.getByPlaceholder('如：上海对外经贸大学博物馆').fill('高校数字文博实验室')
  await page.getByRole('button', { name: '提交存证' }).click()

  await expect(page.getByText('资产发行链路已确认')).toBeVisible()
  await page.getByRole('button', { name: '查看资产详情' }).click()
  await expect(page.getByText('生命周期时间轴')).toBeVisible()
  await expect(page.getByText(/CHAIN_CONFIRMED/)).toBeVisible()

  await page.getByRole('button', { name: '授权管理' }).click()
  const assetCode = await page.locator('input[placeholder^="资产编号"]').inputValue()
  await page.getByLabel('被授权机构').fill('高校数字文博联合展（演示）')
  await page.getByRole('button', { name: '提交授权' }).click()
  await expect(page.getByText('ACTIVE')).toBeVisible()

  await page.getByText('全链路核验', { exact: true }).click()
  await page.getByLabel('资产编号').fill(assetCode)
  await page.getByRole('button', { name: '核验' }).click()
  await expect(page.getByText(`核验结果：${assetCode}`)).toBeVisible()
  await expect(page.getByText(/AUTHORIZATION_CREATED/)).toBeVisible()
})
